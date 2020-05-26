package xyz.pokkst.bagelwallet.ui.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.transaction_item_expanded_sent.view.*
import org.bitcoinj.core.*
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptException
import org.bitcoinj.script.ScriptPattern
import xyz.pokkst.bagelwallet.R
import xyz.pokkst.bagelwallet.util.PriceHelper
import xyz.pokkst.bagelwallet.wallet.WalletManager
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


/**
 * A placeholder fragment containing a simple view.
 */
class TransactionSentFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.transaction_item_expanded_sent, container, false)
        val txid = arguments?.getString("txid", "")
        val tx = WalletManager.walletKit?.wallet()?.getTransaction(Sha256Hash.wrap(txid))
        root.tx_hash_text.text = txid

        root.tx_status_text.text = if(tx?.confidence?.depthInBlocks!! > 0) {
            "confirmed in block #${tx.confidence.appearedAtChainHeight}"
        } else {
            "verified, waiting for confirmation"
        }

        val fromAddresses = ArrayList<String>()
        for(x in tx.inputs.indices) {
            fromAddresses.add(getFromAddress(tx.inputs[x].scriptSig, WalletManager.parameters).toString())
        }
        setSentFromAddresses(root.general_tx_from_layout, fromAddresses)

        val toAddresses = ArrayList<String?>()
        val toAmounts = ArrayList<Long>()
        for(x in tx.outputs.indices) {
            if(ScriptPattern.isOpReturn(tx.outputs[x].scriptPubKey)) {
                toAddresses.add("OP_RETURN")
            } else {
                toAddresses.add(tx.outputs[x].scriptPubKey.getToAddress(WalletManager.parameters).toString())
            }

            toAmounts.add(tx.outputs[x].value.value)
        }

        val bchSent = -tx.getValueSentFromMe(WalletManager.walletKit?.wallet()).toPlainString().toDouble()
        val bchFee = tx.fee.toPlainString().toDouble()
        root.tx_to_fee_amount_text.text = resources.getString(R.string.tx_amount_moved, "-${formatBalance(bchFee, "#.########")}")
        root.tx_amount_text.text = resources.getString(R.string.tx_amount_moved, "${formatBalance(bchSent, "#.########")}")
        object : Thread() {
            override fun run() {
                val fiatValue = bchSent * PriceHelper.price
                val feeFiatValue = bchFee * PriceHelper.price
                requireActivity().runOnUiThread {
                    root.tx_to_fee_exchange_text.text = "($-${formatBalance(feeFiatValue, "0.00")})"
                    root.tx_exchange_text.text = "($${formatBalance(fiatValue, "0.00")})"
                    setSentToAddresses(root.general_tx_to_layout, toAddresses, toAmounts)
                }
            }
        }.start()

        return root
    }

    @Throws(ScriptException::class)
    fun getFromAddress(scriptSig: Script, params: NetworkParameters?): Address? {
        return LegacyAddress.fromPubKeyHash(params, Utils.sha256hash160(scriptSig.pubKey))
    }

    fun formatBalance(amount: Double, pattern: String): String {
        val formatter = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
        return formatter.format(amount)
    }

    private fun setSentFromAddresses(
        view: LinearLayout,
        addresses: ArrayList<String>
    ) {
        val inflater = requireActivity().layoutInflater
        for (address in addresses) {
            val addressBlock =
                inflater.inflate(R.layout.transaction_sent_from_addresses, null) as RelativeLayout
            val txFrom =
                addressBlock.findViewById<View>(R.id.tx_from_text) as TextView
            val txFromDescription =
                addressBlock.findViewById<View>(R.id.tx_from_description) as TextView
            //BRAnimator.showCopyBubble(activity, addressBlock, txFrom)
            if (address != null && address.isNotEmpty()) {
                txFrom.text = address
                txFromDescription.text = getString(R.string.wallet_address)
                view.addView(addressBlock)
            }
        }
    }

    private fun setSentToAddresses(
        view: LinearLayout,
        addresses: ArrayList<String?>,
        amounts: ArrayList<Long>
    ) {
        val inflater = requireActivity().layoutInflater
        for (i in addresses.indices) {
            val addressBlock =
                inflater.inflate(R.layout.transaction_sent_to_addresses, null) as RelativeLayout

            val txTo =
                addressBlock.findViewById<View>(R.id.tx_to_text) as TextView
            val txToDescription =
                addressBlock.findViewById<View>(R.id.tx_to_description) as TextView
            val txToAmount =
                addressBlock.findViewById<View>(R.id.tx_to_amount_text) as TextView
            val txToExchange =
                addressBlock.findViewById<View>(R.id.tx_to_exchange_text) as TextView
            if (addresses[i] != null && addresses[i]!!.isNotEmpty()) {
                txTo.text = addresses[i]
                if(addresses[i] == "OP_RETURN") {
                    txToDescription.text = getString(R.string.op_return_address)
                } else {
                    txToDescription.text = getString(R.string.payment_address)
                }
                val amountInBch = amounts[i] / 100000000.0
                val amountInFiat = amountInBch * PriceHelper.price
                txToAmount.text = resources.getString(R.string.tx_amount_moved, "-${formatBalance(amountInBch, "#.########")}")
                txToExchange.text = "($-${formatBalance(amountInFiat, "0.00")})"
                view.addView(addressBlock)
            }
        }
    }
}