package xyz.pokkst.bagelwallet.ui.main.fragment

import android.app.Activity
import android.content.*
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_new_user.view.*
import kotlinx.android.synthetic.main.fragment_send_home.view.*
import org.bitcoinj.core.Address
import xyz.pokkst.bagelwallet.R
import xyz.pokkst.bagelwallet.qr.QRHelper
import xyz.pokkst.bagelwallet.util.Constants
import xyz.pokkst.bagelwallet.wallet.WalletManager

/**
 * A placeholder fragment containing a simple view.
 */
class SendHomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_send_home, container, false)

        root.scan_qr_code_button.setOnClickListener {
            QRHelper().startQRScan(this, Constants.REQUEST_CODE_SCAN_QR)
        }

        root.paste_address_button.setOnClickListener {
            val clipBoard= requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val pasteData = clipBoard.primaryClip?.getItemAt(0)?.text.toString()
            if(pasteData.contains("http") || Address.isValidCashAddr(WalletManager.parameters, pasteData) || Address.isValidLegacyAddress(WalletManager.parameters, pasteData)) {
                findNavController().navigate(SendHomeFragmentDirections.navToSend(pasteData))
            }
        }
        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.REQUEST_CODE_SCAN_QR) {
                if (data != null) {
                    val scanData = data.getStringExtra(Constants.QR_SCAN_RESULT)
                    if(scanData.contains("http") || Address.isValidCashAddr(WalletManager.parameters, scanData) || Address.isValidLegacyAddress(WalletManager.parameters, scanData)) {
                        findNavController().navigate(SendHomeFragmentDirections.navToSend(scanData))
                    }
                }
            }
        }
    }
}