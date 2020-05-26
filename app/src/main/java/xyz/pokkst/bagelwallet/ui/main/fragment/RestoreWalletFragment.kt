package xyz.pokkst.bagelwallet.ui.main.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.common.base.Splitter
import kotlinx.android.synthetic.main.fragment_restore_wallet.view.*
import xyz.pokkst.bagelwallet.MainActivity
import xyz.pokkst.bagelwallet.R

/**
 * A placeholder fragment containing a simple view.
 */
class RestoreWalletFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_restore_wallet, container, false)

        root.continue_button.setOnClickListener {
            val seedStr = root.recover_wallet_edit_text.text.toString().trim()
            val length = Splitter.on(' ').splitToList(seedStr).size
            if(length == 12) {
                val intent = Intent(requireActivity(), MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                println(seedStr)
                intent.putExtra("seed", seedStr)
                intent.putExtra("new", false)
                startActivity(intent)
            }
        }
        return root
    }
}