package xyz.pokkst.bagelwallet

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import xyz.pokkst.bagelwallet.ui.main.SectionsPagerAdapter
import java.io.File

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_splash)
        object : Thread() {
            override fun run() {
                Looper.prepare()
                try {
                    sleep(1500)
                } catch (ie: InterruptedException) {
                    // fail silently
                }

                val newUser = !File(applicationInfo.dataDir, "bagelwallet.wallet").exists()
                val intent = if(newUser) {
                    Intent(this@SplashActivity, NewUserActivity::class.java)
                } else {
                    Intent(this@SplashActivity, MainActivity::class.java)
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

                Looper.loop()
            }
        }.start()

    }
}