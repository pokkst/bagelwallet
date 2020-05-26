package xyz.pokkst.bagelwallet.ui.main.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.android.synthetic.main.component_input_numpad.view.*
import kotlinx.android.synthetic.main.fragment_send_amount.view.*
import org.bitcoinj.core.Coin
import org.bitcoinj.core.InsufficientMoneyException
import org.bitcoinj.core.Transaction
import org.bitcoinj.wallet.SendRequest
import org.bitcoinj.wallet.Wallet
import xyz.pokkst.bagelwallet.MainActivity
import xyz.pokkst.bagelwallet.R
import xyz.pokkst.bagelwallet.util.Constants
import xyz.pokkst.bagelwallet.util.PriceHelper
import xyz.pokkst.bagelwallet.util.Toaster
import xyz.pokkst.bagelwallet.wallet.WalletManager
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


/**
 * A placeholder fragment containing a simple view.
 */
class SendAmountFragment : Fragment() {
    //val args: SendAmountFragment by navArgs()
    var address: String? = null
    var root: View? = null
    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Constants.ACTION_MAIN_ENABLE_PAGER == intent.action) {
                this@SendAmountFragment.findNavController().popBackStack(R.id.sendHomeFragment, false)
            } else if (Constants.ACTION_FRAGMENT_SEND_SEND == intent.action) {
                this@SendAmountFragment.send()
            }
        }
    }

    var bchIsSendType = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_send_amount, container, false)
        (requireActivity() as MainActivity).enableSendScreen()
        val filter = IntentFilter()
        filter.addAction(Constants.ACTION_MAIN_ENABLE_PAGER)
        filter.addAction(Constants.ACTION_FRAGMENT_SEND_SEND)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, filter)
        root?.input_type_toggle?.isChecked = true
        root?.input_type_toggle?.setOnClickListener {
            bchIsSendType = !bchIsSendType
            swapSendTypes(root)
        }

        val charInputListener = View.OnClickListener { v ->
            val view = v as Button
            appendCharacterToInput(root, view.text.toString())
            updateAltCurrencyDisplay(root)
        }

        root?.input_0?.setOnClickListener(charInputListener)
        root?.input_1?.setOnClickListener(charInputListener)
        root?.input_2?.setOnClickListener(charInputListener)
        root?.input_3?.setOnClickListener(charInputListener)
        root?.input_4?.setOnClickListener(charInputListener)
        root?.input_5?.setOnClickListener(charInputListener)
        root?.input_6?.setOnClickListener(charInputListener)
        root?.input_7?.setOnClickListener(charInputListener)
        root?.input_8?.setOnClickListener(charInputListener)
        root?.input_9?.setOnClickListener(charInputListener)
        root?.decimal_button?.setOnClickListener(charInputListener)
        root?.delete_button?.setOnClickListener {
            val newValue = root?.send_amount_input?.text.toString().dropLast(1)
            root?.send_amount_input?.setText(newValue)
            updateAltCurrencyDisplay(root)
        }

        address = arguments?.getString("address", "")
        root?.to_field_text?.text = "to: ${address?.replace("bitcoincash:", "")}"
        return root
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
    }

    private fun send() {
        if(WalletManager.walletKit?.wallet()?.getBalance(Wallet.BalanceType.ESTIMATED)?.isZero == false) {
            var bchToSend = if(bchIsSendType) {
                root?.send_amount_input?.text.toString()
            } else {
                root?.alt_currency_display?.text.toString()
            }

            bchToSend = formatBalance(bchToSend.toDouble(), "#.########")
            val coinToSend = Coin.parseCoin(bchToSend)

            object : Thread() {
                override fun run() {
                    try {
                        val req: SendRequest = if (coinToSend >= WalletManager.getBalance(WalletManager.walletKit!!.wallet())) {
                            SendRequest.emptyWallet(WalletManager.parameters, address)
                        } else {
                            SendRequest.to(WalletManager.parameters, address, coinToSend)
                        }

                        req.allowUnconfirmed()
                        req.ensureMinRequiredFee = false
                        req.feePerKb = Coin.valueOf(2L * 1000L)
                        val sendResult = WalletManager.walletKit?.wallet()?.sendCoins(req)
                        Futures.addCallback(sendResult?.broadcastComplete,
                            object : FutureCallback<Transaction?> {
                                override fun onSuccess(@Nullable result: Transaction?) {
                                    Toaster.showMessage(requireActivity() as MainActivity, "coins sent!")
                                    (requireActivity() as MainActivity).disableSendScreen()
                                }

                                override fun onFailure(t: Throwable) { // We died trying to empty the wallet.

                                }
                            },
                            MoreExecutors.directExecutor()
                        )
                    }  catch (e: InsufficientMoneyException) {
                        e.printStackTrace()
                        Toaster.showMessage(requireActivity() as MainActivity, "not enough coins in wallet")
                    } catch (e: Wallet.CouldNotAdjustDownwards) {
                        e.printStackTrace()
                        Toaster.showMessage(requireActivity() as MainActivity, "error adjusting downwards")
                    } catch (e: Wallet.ExceededMaxTransactionSize) {
                        e.printStackTrace()
                        Toaster.showMessage(requireActivity() as MainActivity, "transaction is too big")
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                        e.message?.let { Toaster.showMessage(requireActivity() as MainActivity, it) }
                    }
                }
            }.start()
        } else {
            Toaster.showMessage(requireActivity() as MainActivity, "wallet balance is zero")
        }
    }

    private fun swapSendTypes(root: View?) {
        if(root != null) {
            if (bchIsSendType) {
                //We are changing from BCH as the alt currency.
                val bchValue = root.alt_currency_display.text.toString()
                val fiatValue = root.send_amount_input.text.toString()
                root.main_currency_symbol.text = resources.getString(R.string.b_symbol)
                root.alt_currency_symbol.text = resources.getString(R.string.fiat_symbol)
                root.send_amount_input.setText(bchValue)
                root.alt_currency_display.text = fiatValue
            } else {
                //We are changing from fiat as the alt currency.
                val bchValue = root.send_amount_input.text.toString()
                val fiatValue = root.alt_currency_display.text.toString()
                root.main_currency_symbol.text = resources.getString(R.string.fiat_symbol)
                root.alt_currency_symbol.text = resources.getString(R.string.b_symbol)
                root.alt_currency_display.text = bchValue
                root.send_amount_input.setText(fiatValue)
            }
        }
    }

    private fun appendCharacterToInput(root: View?, char: String) {
        if(root != null) {
            if (char == "." && !root.send_amount_input.text.toString().contains(".")) {
                root.send_amount_input.append(char)
            } else if (char != ".") {
                root.send_amount_input.append(char)
            }
        }
    }

    private fun updateAltCurrencyDisplay(root: View?) {
        if(root != null) {
            object : Thread() {
                override fun run() {
                    if (root.send_amount_input.text.toString().isNotEmpty()) {
                        if (bchIsSendType) {
                            val bchValue =
                                java.lang.Double.parseDouble(root.send_amount_input.text.toString())
                            val price = PriceHelper.price
                            val fiatValue = bchValue * price
                            requireActivity().runOnUiThread {
                                root.alt_currency_display.text = formatBalance(fiatValue, "0.00")
                            }
                        } else {
                            val fiatValue =
                                java.lang.Double.parseDouble(root.send_amount_input.text.toString())
                            val price = PriceHelper.price
                            val bchValue = fiatValue / price
                            requireActivity().runOnUiThread {
                                root.alt_currency_display.text =
                                    formatBalance(bchValue, "#.########")
                            }
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            root.alt_currency_display.text = null
                        }
                    }
                }
            }.start()
        }
    }

    fun formatBalance(amount: Double, pattern: String): String {
        val formatter = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
        return formatter.format(amount)
    }

}