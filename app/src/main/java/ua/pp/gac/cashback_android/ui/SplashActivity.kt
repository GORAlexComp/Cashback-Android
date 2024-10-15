package ua.pp.gac.cashback_android.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ua.pp.gac.cashback_android.R

@SuppressLint("CustomSplashScreen")
@Suppress("DEPRECATION")
class SplashActivity : AppCompatActivity() {
	@SuppressLint("MissingInflatedId", "SetTextI18n")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_splash)

		val logo: ImageView = findViewById(R.id.splash_logo)
		val version: TextView = findViewById(R.id.version)

		val pm = packageManager.getPackageInfo(packageName, 0)
		version.text =
			getString(R.string.version).toString() + "${pm.versionName} (${pm.versionCode})"

		android.os.Handler().postDelayed({
			val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
			logo.startAnimation(animation)

			startActivity(android.content.Intent(this, MainActivity::class.java))
			overridePendingTransition(R.anim.fade_in, 0)
			finish()
		}, 2000)
	}
}
