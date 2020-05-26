package xyz.pokkst.bagelwallet.ui.main

import android.R.attr.label
import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.zxing.WriterException
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import net.glxn.qrgen.android.QRCode
import xyz.pokkst.bagelwallet.R
import xyz.pokkst.bagelwallet.util.Constants
import xyz.pokkst.bagelwallet.util.Toaster


/**
 * A placeholder fragment containing a simple view.
 */
class MainFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var page: Int = 0
    var receiveQr: ImageView? = null
    var receiveText: TextView? = null

    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Constants.ACTION_UPDATE_RECEIVE_QR == intent.action) {
                val address = intent.extras?.getString("address")
                this@MainFragment.refresh(address)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
            page = arguments?.getInt(ARG_SECTION_NUMBER) ?: 1
        }

        val filter = IntentFilter()
        filter.addAction(Constants.ACTION_UPDATE_RECEIVE_QR)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, filter)

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main, container, false)
        val sendScreen: LinearLayout = root.findViewById(R.id.send_screen)
        val receiveScreen: LinearLayout = root.findViewById(R.id.receive_screen)
        if(page == 1) {
            sendScreen.visibility = View.VISIBLE
            receiveScreen.visibility = View.GONE
        } else if(page == 2) {
            receiveQr = root.findViewById(R.id.receive_qr)
            receiveText = root.findViewById(R.id.main_address_text)
            receiveText?.setOnClickListener {
                copyToClipboard(receiveText?.text.toString())
            }
            receiveQr?.setOnClickListener {
                copyToClipboard(receiveText?.text.toString())
            }
            sendScreen.visibility = View.GONE
            receiveScreen.visibility = View.VISIBLE
        }

        return root
    }

    private fun copyToClipboard(text: String) {
        val clipboard: ClipboardManager? = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("Address", text)
        clipboard?.setPrimaryClip(clip)
        Toaster.showToastMessage(requireContext(), "copied")
    }
    private fun refresh(address: String?) {
        this.generateQR(address)
    }
    private fun generateQR(address: String?) {
        try {
            val encoder = QRCode.from(address).withSize(1024, 1024).withErrorCorrection(ErrorCorrectionLevel.H)

            val qrCode = encoder.bitmap()
            receiveQr?.setImageBitmap(qrCode)
            receiveText?.text = address
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): MainFragment {
            return MainFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}