package xyz.pokkst.bagelwallet

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.listeners.DownloadProgressTracker
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.utils.Threading
import org.bitcoinj.wallet.DeterministicSeed
import xyz.pokkst.bagelwallet.ui.ToggleViewPager
import xyz.pokkst.bagelwallet.ui.main.SectionsPagerAdapter
import xyz.pokkst.bagelwallet.util.Constants
import xyz.pokkst.bagelwallet.util.PriceHelper
import xyz.pokkst.bagelwallet.wallet.WalletManager
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Constants.ACTION_UPDATE_RECEIVE_QR == intent.action) {
                if(intent.extras?.containsKey("sync") == true) {
                    val pct = intent.extras?.getInt("sync")
                    this@MainActivity.refresh(pct)
                } else {
                    this@MainActivity.refresh()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val extras = intent.extras
        var seed: String? = null
        var newUser: Boolean = false
        if (extras != null) {
            seed = extras.getString("seed")
            newUser = extras.getBoolean("new")
        }
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ToggleViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        if(newUser) { viewPager.currentItem = 2 }
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        val settingsButton: ImageView = findViewById(R.id.settings_button)
        settingsButton.setOnClickListener {
            if(viewPager.isPagingEnabled()) {
                val intentSettings = Intent(this, SettingsActivity::class.java)
                startActivity(intentSettings)
            } else {
                disableSendScreen()
            }
        }

        pay_button.setOnClickListener {
            pay_button.isEnabled = false
            val intent = Intent(Constants.ACTION_FRAGMENT_SEND_SEND)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }

        val filter = IntentFilter()
        filter.addAction(Constants.ACTION_UPDATE_RECEIVE_QR)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

        WalletManager.startWallet(this, seed)
    }

    private fun refresh(sync: Int?) {
        if(WalletManager.walletKit?.wallet() != null) {
            object : Thread() {
                override fun run() {
                    super.run()
                    val bch = WalletManager.getBalance(WalletManager.walletKit?.wallet()!!).toPlainString().toDouble()
                    val fiat = bch * PriceHelper.price
                    val fiatStr = formatBalance(fiat, "0.00")
                    this@MainActivity.runOnUiThread {
                        appbar_title.text = "${resources.getString(R.string.appbar_title, bch)} ($${fiatStr})"
                    }
                }
            }.start()
        }
        if (sync != null) {
            sync_progress_bar.visibility = if(sync == 100) View.INVISIBLE else View.VISIBLE
            sync_progress_bar.progress = sync
        }
    }

    private fun refresh() {
        if(WalletManager.walletKit?.wallet() != null) {
            object : Thread() {
                override fun run() {
                    super.run()
                    val bch = WalletManager.getBalance(WalletManager.walletKit?.wallet()!!).toPlainString().toDouble()
                    val fiat = bch * PriceHelper.price
                    val fiatStr = formatBalance(fiat, "0.00")
                    this@MainActivity.runOnUiThread {
                        appbar_title.text = "${resources.getString(R.string.appbar_title, bch)} ($${fiatStr})"
                    }
                }
            }.start()
        }
    }

    fun enableSendScreen() {
        val viewPager: ToggleViewPager = findViewById(R.id.view_pager)
        val tabs: TabLayout = findViewById(R.id.tabs)
        viewPager.setPagingEnabled(false)
        settings_button.setImageResource(R.drawable.navigationback)
        pay_button.visibility = View.VISIBLE
        pay_button.isEnabled = true
        tabs.visibility = View.GONE
    }

    fun disableSendScreen() {
        val viewPager: ToggleViewPager = findViewById(R.id.view_pager)
        val tabs: TabLayout = findViewById(R.id.tabs)
        viewPager.setPagingEnabled(true)
        settings_button.setImageResource(R.drawable.burger)
        tabs.visibility = View.VISIBLE
        pay_button.visibility = View.GONE
        val intent = Intent(Constants.ACTION_MAIN_ENABLE_PAGER)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun formatBalance(amount: Double, pattern: String): String {
        val formatter = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
        return formatter.format(amount)
    }
}