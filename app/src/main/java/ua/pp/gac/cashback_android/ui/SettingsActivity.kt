package ua.pp.gac.cashback_android.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import ua.pp.gac.cashback_android.MoreFunctions
import ua.pp.gac.cashback_android.R
import ua.pp.gac.cashback_android.UpdatesData
import ua.pp.gac.cashback_android.database.DatabaseHelper
import ua.pp.gac.cashback_android.database.SettingsTable

class SettingsActivity : AppCompatActivity() {
	private lateinit var dbHelper: DatabaseHelper

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_settings)

		val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
		toolbarTitle.text = getString(R.string.settings_title)

		val btnBack = findViewById<ImageButton>(R.id.back)
		btnBack.setOnClickListener {
			onBackPressedDispatcher.onBackPressed()
		}

		dbHelper = DatabaseHelper(this)

		val updatesDataBtn: MaterialButton = findViewById(R.id.btn_update_contents)
		updatesDataBtn.setOnClickListener {
			UpdatesData(this, dbHelper.readableDatabase).startUpdate()
		}

		val switchSaveNet: MaterialSwitch = findViewById(R.id.switch_save_net)
		switchSaveNet.isChecked = SettingsTable.isEconomyMode(dbHelper.readableDatabase) == true
		switchSaveNet.setOnCheckedChangeListener { _, isChecked ->
			SettingsTable.setEconomyMode(dbHelper.writableDatabase, isChecked)
		}

		val spinnerThemeApp = findViewById<Spinner>(R.id.spinner_theme_app)
		val spinnerThemeAppAdapter = ArrayAdapter.createFromResource(
			this,
			R.array.theme_app,
			android.R.layout.simple_spinner_item
		)
		spinnerThemeAppAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
		spinnerThemeApp.adapter = spinnerThemeAppAdapter

		val savedThemeApp = SettingsTable.getThemeMode(dbHelper.readableDatabase)
		spinnerThemeApp.setSelection(savedThemeApp)

		spinnerThemeApp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onItemSelected(
				parent: AdapterView<*>,
				view: View?,
				position: Int,
				id: Long,
			) {
				SettingsTable.setThemeMode(dbHelper.writableDatabase, position)
				val mf = MoreFunctions(this@SettingsActivity)
				mf.applyTheme(position)
			}

			override fun onNothingSelected(parent: AdapterView<*>) {
				/* empty */
			}
		}
	}
}
