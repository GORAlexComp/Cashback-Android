package ua.pp.gac.cashback_android.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import ua.pp.gac.cashback_android.R
import ua.pp.gac.cashback_android.ui.ProductActivity
import ua.pp.gac.cashback_android.ui.RetailerActivity
import ua.pp.gac.cashback_android.ui.ScannerActivity

class WidgetSearchProvider : AppWidgetProvider() {
	companion object {
		fun updateSearchWidget(
			context: Context,
			appWidgetManager: AppWidgetManager,
			appWidgetId: Int,
		) {
			val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
			val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)

			val layoutId = when {
				minWidth < 150 -> R.layout.widget_search_1
				minWidth < 225 -> R.layout.widget_search_2
				else -> R.layout.widget_search_3
			}

			val views = RemoteViews(context.packageName, layoutId)

			views.setOnClickPendingIntent(
				R.id.open_scanner,
				getPendingIntent(context, ScannerActivity::class.java)
			)
			views.setOnClickPendingIntent(
				R.id.open_products,
				getPendingIntent(context, ProductActivity::class.java)
			)
			views.setOnClickPendingIntent(
				R.id.open_retailers,
				getPendingIntent(context, RetailerActivity::class.java)
			)

			appWidgetManager.updateAppWidget(appWidgetId, views)
		}

		private fun getPendingIntent(context: Context, activityClass: Class<*>): PendingIntent {
			val intent = Intent(context, activityClass).apply {
				flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
			}
			return PendingIntent.getActivity(
				context,
				0,
				intent,
				PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
			)
		}
	}

	override fun onUpdate(
		context: Context,
		appWidgetManager: AppWidgetManager,
		appWidgetIds: IntArray,
	) {
		for (appWidgetId in appWidgetIds) {
			updateSearchWidget(context, appWidgetManager, appWidgetId)
		}
	}

	override fun onAppWidgetOptionsChanged(
		context: Context,
		appWidgetManager: AppWidgetManager,
		appWidgetId: Int,
		newOptions: Bundle,
	) {
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)

		updateSearchWidget(context, appWidgetManager, appWidgetId)
	}
}