package xyz.pokkst.bagelwallet.wallet

import android.app.Activity
import android.content.Intent
import android.os.Handler
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.bitcoinj.core.Coin
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.listeners.DownloadProgressTracker
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.utils.Threading
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.Wallet
import xyz.pokkst.bagelwallet.util.Constants
import java.io.File
import java.util.*
import java.util.concurrent.Executor

class WalletManager {
    companion object {
        lateinit var walletDir: File
        var walletKit: WalletAppKit? = null
        val parameters: NetworkParameters = MainNetParams.get()
        private val walletFileName = "bagelwallet"
        fun startWallet(activity: Activity, seed: String?) {
            walletDir = File(activity.applicationInfo.dataDir)
            setBitcoinSDKThread()

            walletKit = object : WalletAppKit(parameters, walletDir, walletFileName) {
                override fun onSetupCompleted() {
                    println("STARTED wallet...")
                    wallet().isAcceptRiskyTransactions = true
                    refresh(activity, 0)
                    wallet().addCoinsReceivedEventListener { wallet, tx, prevBalance, newBalance ->
                        refresh(activity, null)
                    }
                    wallet().addCoinsSentEventListener { wallet, tx, prevBalance, newBalance ->
                        refresh(activity, null)
                    }
                }
            }

            walletKit?.setDownloadListener(object : DownloadProgressTracker() {
                override fun doneDownload() {
                    super.doneDownload()
                    println("DONE DOWNLOAD")
                    //sync_progress_bar.visibility = View.INVISIBLE
                    refresh(activity, 100)
                }
                override fun progress(pct: Double, blocksSoFar: Int, date: Date?) {
                    super.progress(pct, blocksSoFar, date)
                    refresh(activity, pct.toInt())
                    println(pct)
                    //sync_progress_bar.progress = pct.toInt()
                }
            })

            if (seed != null) {
                val deterministicSeed = DeterministicSeed(seed, null, "", 1554163098L)
                walletKit?.restoreWalletFromSeed(deterministicSeed)
            }

            walletKit?.setBlockingStartup(false)
            val checkpointsInputStream = activity.assets.open("checkpoints.txt")
            walletKit?.setCheckpoints(checkpointsInputStream)

            println("Starting wallet...")
            walletKit?.startAsync()
        }

        fun getBalance(wallet: Wallet): Coin {
            return wallet.getBalance(Wallet.BalanceType.ESTIMATED)
        }


        fun setBitcoinSDKThread() {
            val handler = Handler()
            Threading.USER_THREAD = Executor { handler.post(it) }
        }

        fun refresh(activity: Activity, sync: Int?) {
            val intent = Intent(Constants.ACTION_UPDATE_RECEIVE_QR)
            intent.putExtra("address", walletKit?.wallet()?.currentReceiveAddress()?.legacyAddress?.toBase58())
            intent.putExtra("sync", sync)
            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent)
        }
    }
}