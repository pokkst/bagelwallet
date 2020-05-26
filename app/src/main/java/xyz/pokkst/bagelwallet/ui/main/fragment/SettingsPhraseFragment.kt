package xyz.pokkst.bagelwallet.ui.main.fragment

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
import org.bitcoinj.wallet.DeterministicSeed
import org.bitcoinj.wallet.Wallet
import xyz.pokkst.bagelwallet.R
import xyz.pokkst.bagelwallet.util.DateFormatter
import xyz.pokkst.bagelwallet.wallet.WalletManager
import java.security.SecureRandom


/**
 * A placeholder fragment containing a simple view.
 */
class SettingsPhraseFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_settings_phrase, container, false)
        root.the_phrase.text = WalletManager.walletKit?.wallet()?.keyChainSeed?.mnemonicString
        return root
    }
}