package xyz.pokkst.bagelwallet.util

import android.app.Activity
import android.content.Context
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import xyz.pokkst.bagelwallet.MainActivity

class Toaster {
    companion object {
        fun showToastMessage(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun showMessage(activity: MainActivity, message: String) {
            activity.runOnUiThread {
                activity.pay_button.isEnabled = true
                showToastMessage(activity, message)
            }
        }
    }
}