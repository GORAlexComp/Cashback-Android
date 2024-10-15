package ua.pp.gac.cashback_android

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import ua.pp.gac.cashback_android.widget.WidgetCountProvider
import ua.pp.gac.cashback_android.widget.WidgetSearchProvider

class AppWidgetConfig : Activity() {
	private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.widget_config)

		val intent = intent
		val extras = intent.extras
		if (extras != null) {
			appWidgetId = extras.getInt(
				AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID
			)

			if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
				finish()
				return
			}

			val widgetTypeGroup = findViewById<RadioGroup>(R.id.type_widget_group)
			val btnConfirm = findViewById<Button>(R.id.btn_confirm)

			btnConfirm.setOnClickListener {
				val selectedWidgetType = when (widgetTypeGroup.checkedRadioButtonId) {
					R.id.type_widget_search -> 1
//					R.id.type_widget_count -> 2
					else -> 1
				}

				saveWidgetType(this, appWidgetId, selectedWidgetType)

				val result = Intent()
				result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
				setResult(RESULT_OK, result)

				val appWidgetManager = AppWidgetManager.getInstance(this)
				WidgetSearchProvider.updateSearchWidget(this, appWidgetManager, appWidgetId)
				WidgetCountProvider.updateCountWidget(this, appWidgetManager, appWidgetId)

				finish()
			}
		}
	}

	fun saveWidgetType(context: Context, appWidgetId: Int, widgetType: Int) {
		val prefs = context.getSharedPreferences("widget_prefs", MODE_PRIVATE)
		with(prefs.edit()) {
			putInt("widget_type_$appWidgetId", widgetType)
			apply()
		}
	}
}