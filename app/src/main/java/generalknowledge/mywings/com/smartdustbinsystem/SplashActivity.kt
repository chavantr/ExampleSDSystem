package generalknowledge.mywings.com.smartdustbinsystem

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import generalknowledge.mywings.com.smartdustbinsystem.process.OnSplashCompleteListener
import generalknowledge.mywings.com.smartdustbinsystem.process.SplashWaitAsync
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : AppCompatActivity(), OnSplashCompleteListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.window
            .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        SplashWaitAsync().setOnFlashListener(this, 0)

        ObjectAnimator.ofFloat(lblSmartCity, "translationY", 70f).apply {
            duration = 2000
            start()
        }

        ObjectAnimator.ofFloat(lblCleanCity, "translationY", -70f).apply {
            duration = 2000
            start()
        }
    }

    override fun onSplashComplete(result: Int) {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
