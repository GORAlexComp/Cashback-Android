package ua.pp.gac.cashback_android

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import ua.pp.gac.cashback_android.widget.WidgetCountProvider

@Suppress("DEPRECATION")
class MoreFunctions(val context: Context) {
	fun applyTheme(theme: Int) {
		when (theme) {
			0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
			1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
			2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
		}
	}

	fun updateCountWidget(context: Context) {
		val intent = Intent(context, WidgetCountProvider::class.java)
		intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
		val ids = AppWidgetManager.getInstance(context)
			.getAppWidgetIds(ComponentName(context, WidgetCountProvider::class.java))
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
		context.sendBroadcast(intent)
	}
}
