package xyz.pokkst.bagelwallet.ui.main.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_generated_seed.view.*
import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.crypto.MnemonicException
import org.bitcoinj.wallet.DeterministicSeed
import xyz.pokkst.bagelwallet.MainActivity
import xyz.pokkst.bagelwallet.R
import java.security.SecureRandom


/**
 * A placeholder fragment containing a simple view.
 */
class GeneratedSeedFragment : Fragment() {
    private val entropy: ByteArray
        get() = getEntropy(SecureRandom())

    private fun getEntropy(random: SecureRandom): ByteArray {
        val seed = ByteArray(DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS / 8)
        random.nextBytes(seed)
        return seed
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_generated_seed, container, false)

        val entropy = entropy
        var mnemonic: List<String>? = null
        try {
            mnemonic = MnemonicCode.INSTANCE.toMnemonic(entropy)
        } catch (e: MnemonicException.MnemonicLengthException) {
            e.printStackTrace()
        }

        val mnemonicCode = mnemonic
        val recoverySeed = StringBuilder()

        assert(mnemonicCode != null)
        for (x in mnemonicCode!!.indices) {
            recoverySeed.append(mnemonicCode[x]).append(if (x == mnemonicCode.size - 1) "" else " ")
        }

        val seedStr = recoverySeed.toString()

        root.the_phrase.text = seedStr

        root.continue_button.setOnClickListener {
            val intent = Intent(requireActivity(), MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            println(seedStr)
            intent.putExtra("seed", seedStr)
            intent.putExtra("new", true)
            startActivity(intent)
        }
        return root
    }
}