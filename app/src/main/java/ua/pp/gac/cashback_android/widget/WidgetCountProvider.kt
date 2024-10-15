package ua.pp.gac.cashback_android.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import ua.pp.gac.cashback_android.R
import ua.pp.gac.cashback_android.database.DatabaseHelper
import ua.pp.gac.cashback_android.database.ProductsTable
import ua.pp.gac.cashback_android.database.RetailersTable

class WidgetCountProvider : AppWidgetProvider() {
	override fun onUpdate(
		context: Context,
		appWidgetManager: AppWidgetManager,
		appWidgetIds: IntArray,
	) {
		for (appWidgetId in appWidgetIds) {
			updateCountWidget(context, appWidgetManager, appWidgetId)
		}
	}

	companion object {
		fun updateCountWidget(
			context: Context,
			appWidgetManager: AppWidgetManager,
			appWidgetId: Int,
		) {
			val dbHelper = DatabaseHelper(context)

			val views = RemoteViews(context.packageName, R.layout.widget_count)
			val countProducts = ProductsTable.getProductsCount(dbHelper.readableDatabase)
			val countRetailers = RetailersTable.getRetailersCount(dbHelper.readableDatabase)

			views.setTextViewText(R.id.products_count, countProducts.toString())
			views.setTextViewText(R.id.retailers_count, countRetailers.toString())

			appWidgetManager.updateAppWidget(appWidgetId, views)
		}
	}
}