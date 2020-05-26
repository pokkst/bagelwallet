package xyz.pokkst.bagelwallet

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_settings.appbar_title
import kotlinx.android.synthetic.main.activity_settings.settings_button
import org.bitcoinj.core.Utils
import org.bitcoinj.wallet.Wallet
import xyz.pokkst.bagelwallet.util.Constants
import xyz.pokkst.bagelwallet.util.DateFormatter
import xyz.pokkst.bagelwallet.util.PriceHelper
import xyz.pokkst.bagelwallet.wallet.WalletManager
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


class SettingsActivity : AppCompatActivity() {
    var deepMenuCount = 0
    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Constants.ACTION_SETTINGS_HIDE_BAR == intent.action) {
                this@SettingsActivity.showDeepMenuBar()
            } else if (Constants.ACTION_SETTINGS_SHOW_BAR == intent.action) {
                this@SettingsActivity.showRootMenuBar()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val settingsButton: ImageView = findViewById(R.id.settings_button)
        settingsButton.setOnClickListener {
            if(deepMenuCount > 0) {
                onBackPressed()
            } else {
                finish()
            }
        }

        showRootMenuBar()

        val filter = IntentFilter()
        filter.addAction(Constants.ACTION_SETTINGS_HIDE_BAR)
        filter.addAction(Constants.ACTION_SETTINGS_SHOW_BAR)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if(deepMenuCount > 0)
            handleDeepMenu()
    }

    private fun handleDeepMenu() {
        deepMenuCount--

        if(deepMenuCount <= 0) {
            deepMenuCount = 0
            showRootMenuBar()
        }
    }
    private fun showDeepMenuBar() {
        deepMenuCount++
        appbar_title.text = resources.getString(R.string.app_name)
        settings_button.setImageResource(R.drawable.navigationback)
    }

    private fun showRootMenuBar() {
        deepMenuCount = 0
        object : Thread() {
            override fun run() {
                if(WalletManager.walletKit?.wallet() != null) {
                    val bch = WalletManager.getBalance(WalletManager.walletKit?.wallet()!!)
                        .toPlainString().toDouble()
                    val fiat = bch * PriceHelper.price
                    val fiatStr = formatBalance(fiat, "0.00")
                    this@SettingsActivity.runOnUiThread {
                        appbar_title.text =
                            "${resources.getString(R.string.appbar_title, bch)} ($${fiatStr})"
                    }
                }
            }
        }.start()
        settings_button.setImageResource(R.drawable.x)
    }

    fun formatBalance(amount: Double, pattern: String): String {
        val formatter = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
        return formatter.format(amount)
    }
}