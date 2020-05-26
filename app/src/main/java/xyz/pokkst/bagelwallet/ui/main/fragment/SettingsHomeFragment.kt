package xyz.pokkst.bagelwallet.ui.main.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_settings_home.view.*
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.Wallet
import xyz.pokkst.bagelwallet.R
import xyz.pokkst.bagelwallet.util.Constants
import xyz.pokkst.bagelwallet.util.DateFormatter
import xyz.pokkst.bagelwallet.util.PriceHelper
import xyz.pokkst.bagelwallet.wallet.WalletManager
import java.security.SecureRandom
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * A placeholder fragment containing a simple view.
 */
class SettingsHomeFragment : Fragment() {
    private var sentColor = 0
    private var receivedColor = 0
    private var txList = ArrayList<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_settings_home, container, false)
        sentColor = Color.parseColor("#FF5454")
        receivedColor = Color.parseColor("#00BF00")
        this.setArrayAdapter(root, WalletManager.walletKit?.wallet())

        root.about.setOnClickListener {
            val intent = Intent(Constants.ACTION_SETTINGS_HIDE_BAR)
            LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent)
            findNavController().navigate(R.id.nav_to_about)
        }

        root.recovery_phrase.setOnClickListener {
            val intent = Intent(Constants.ACTION_SETTINGS_HIDE_BAR)
            LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent)
            findNavController().navigate(R.id.nav_to_phrase)
        }

        root.start_recovery_wallet.setOnClickListener {
            val intent = Intent(Constants.ACTION_SETTINGS_HIDE_BAR)
            LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent)
            findNavController().navigate(R.id.nav_to_wipe)
        }

        root.transactions_list.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(Constants.ACTION_SETTINGS_HIDE_BAR)
            LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent)
            val txid = txList[position]
            val amount = WalletManager.walletKit?.wallet()?.getTransaction(Sha256Hash.wrap(txid))?.getValue(WalletManager.walletKit?.wallet())
            if(amount?.isPositive!!) {
                findNavController().navigate(SettingsHomeFragmentDirections.navToTxReceived(txid))
            } else if(amount.isNegative) {
                findNavController().navigate(SettingsHomeFragmentDirections.navToTxSent(txid))
            }
        }

        return root
    }

    private fun setArrayAdapter(root: View, wallet: Wallet?) {
        setListViewShit(root, wallet)
    }

    private fun setListViewShit(root: View, wallet: Wallet?) {
        object : Thread() {
            override fun run() {
                if (wallet != null) {
                    val txListFromWallet = wallet.getRecentTransactions(5, false)
                    txList = ArrayList<String>()

                    if (txListFromWallet != null && txListFromWallet.size != 0) {
                        val txListFormatted = ArrayList<Map<String, String>>()

                        if (txListFromWallet.size > 0) {
                            requireActivity().runOnUiThread {
                                root.no_transactions.visibility = View.GONE
                            }

                            for (x in 0 until txListFromWallet.size) {
                                val tx = txListFromWallet[x]
                                val confirmations = tx.confidence.depthInBlocks
                                val value = tx.getValue(wallet)
                                val timestamp = tx.updateTime.time.toString()
                                val datum = HashMap<String, String>()
                                val amountStr = value.toPlainString()
                                datum["action"] = if(value.isPositive) {
                                    "received"
                                } else {
                                    "sent"
                                }

                                datum["amount"] = amountStr
                                datum["fiatAmount"] = formatBalance((amountStr.toDouble() * PriceHelper.price), "0.00")
                                datum["timestamp"] = timestamp

                                when {
                                    confirmations == 0 -> datum["confirmations"] = "0/unconfirmed"
                                    confirmations < 6 -> datum["confirmations"] = "$confirmations/6 confirmations"
                                    else -> datum["confirmations"] = "6+ confirmations"
                                }

                                txList.add(tx.txId.toString())
                                txListFormatted.add(datum)
                            }

                            val itemsAdapter = object : SimpleAdapter(requireContext(), txListFormatted, R.layout.transaction_list_item, null, null) {
                                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                    // Get the Item from ListView
                                    val view = LayoutInflater.from(requireContext()).inflate(R.layout.transaction_list_item, null)
                                    val sentReceivedTextView = view.findViewById<TextView>(R.id.transaction_sent_received_label)
                                    val dateTextView = view.findViewById<TextView>(R.id.transaction_date)
                                    val bitsMoved = view.findViewById<TextView>(R.id.transaction_amount_bits)
                                    val dollarsMoved = view.findViewById<TextView>(R.id.transaction_amount_dollars)


                                    val action = txListFormatted[position]["action"]
                                    val received = action == "received"
                                    val amount = txListFormatted[position]["amount"]
                                    val fiatAmount = txListFormatted[position]["fiatAmount"]
                                    val confirmations = txListFormatted[position]["confirmations"]
                                    val timestamp = txListFormatted[position]["timestamp"]?.let { java.lang.Long.parseLong(it) }
                                    sentReceivedTextView.setBackgroundResource(if (received) R.drawable.received_label else R.drawable.sent_label)
                                    sentReceivedTextView.setTextColor(if (received) receivedColor else sentColor)
                                    sentReceivedTextView.text = action
                                    bitsMoved.text = resources.getString(R.string.tx_amount_moved, amount)
                                    dollarsMoved.text = "($$fiatAmount)"
                                    dateTextView.text = if (timestamp != 0L) DateFormatter.getFormattedDateFromLong(
                                        requireActivity(),
                                        timestamp!!
                                    ) else DateFormatter.getFormattedDateFromLong(requireActivity(), System.currentTimeMillis())
                                    // Generate ListView Item using TextView
                                    return view
                                }
                            }
                            requireActivity().runOnUiThread { root.transactions_list.adapter = itemsAdapter }
                        } else {
                            requireActivity().runOnUiThread {
                                root.transactions_list.visibility = View.GONE
                                root.no_transactions.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            root.transactions_list.visibility = View.GONE
                            root.no_transactions.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }.start()
    }

    fun formatBalance(amount: Double, pattern: String): String {
        val formatter = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
        return formatter.format(amount)
    }
}