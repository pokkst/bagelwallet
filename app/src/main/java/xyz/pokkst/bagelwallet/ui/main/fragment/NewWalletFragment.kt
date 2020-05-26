package xyz.pokkst.bagelwallet.ui.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_new_wallet.view.*
import kotlinx.android.synthetic.main.intro_fragment_warning.view.*
import xyz.pokkst.bagelwallet.R

/**
 * A placeholder fragment containing a simple view.
 */
class NewWalletFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_new_wallet, container, false)
        root.intro_new_wallet_generate.setOnClickListener {
            if(root.seed_warning_screen.visibility != View.VISIBLE)
                root.seed_warning_screen.visibility = View.VISIBLE
        }

        root.intro_warning_show_button.setOnClickListener {
            findNavController().navigate(R.id.nav_to_generated_seed)
        }
        return root
    }
}