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
import kotlinx.android.synthetic.main.fragment_settings_home.view.*
import kotlinx.android.synthetic.main.fragment_settings_phrase.view.*
import kotlinx.android.synthetic.main.fragment_settings_wipe.view.*
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.Wallet
import xyz.pokkst.bagelwallet.MainActivity
import xyz.pokkst.bagelwallet.NewUserActivity
import xyz.pokkst.bagelwallet.R
import xyz.pokkst.bagelwallet.util.DateFormatter
import xyz.pokkst.bagelwallet.wallet.WalletManager
import java.io.File
import java.security.SecureRandom


/**
 * A placeholder fragment containing a simple view.
 */
class SettingsWipeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_settings_wipe, container, false)
        root.continue_button.setOnClickListener {
            val seedEntered = root.editText_phrase.text.toString().trim()
            val walletSeed = WalletManager.walletKit?.wallet()?.keyChainSeed?.mnemonicString
            if(seedEntered == walletSeed) {
                val walletFile = File(WalletManager.walletDir, "bagelwallet.wallet")
                val spvChainFile = File(WalletManager.walletDir, "bagelwallet.spvchain")
                walletFile.delete()
                spvChainFile.delete()
                val intent = Intent(requireContext(), NewUserActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return root
    }
}