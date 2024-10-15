package ua.pp.gac.cashback_android.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ua.pp.gac.cashback_android.PartnerBanksAdapter
import ua.pp.gac.cashback_android.PartnerBanksAdapter.Bank
import ua.pp.gac.cashback_android.R

class PartnerBankActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_partners_bank)

		val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
		toolbarTitle.text = getString(R.string.bank_partners)

		val btnBack = findViewById<ImageButton>(R.id.back)
		btnBack.setOnClickListener {
			onBackPressedDispatcher.onBackPressed()
		}

		val partnerBanks = listOf(
			Bank("Monobank", "mono.png", "app", "https://mbnk.app")
		)

		val recyclerViewBanks: RecyclerView = findViewById(R.id.banks_list)
		recyclerViewBanks.layoutManager = LinearLayoutManager(this)

		val adapter = PartnerBanksAdapter(partnerBanks) { bank ->
			if (bank.type == "app") {
				val intent = Intent(Intent.ACTION_VIEW, Uri.parse(bank.deep))
				startActivity(intent)
			}
		}

		recyclerViewBanks.adapter = adapter
	}
}
