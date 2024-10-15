package ua.pp.gac.cashback_android.ui

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ua.pp.gac.cashback_android.R

class RulesActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_rules)

		val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
		toolbarTitle.text = getString(R.string.rules)

		val btnBack = findViewById<ImageButton>(R.id.back)
		btnBack.setOnClickListener {
			onBackPressedDispatcher.onBackPressed()
		}
	}
}