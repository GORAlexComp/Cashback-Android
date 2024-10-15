package ua.pp.gac.cashback_android

import android.app.Application
import android.content.Intent
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import ua.pp.gac.cashback_android.ui.ProductActivity
import ua.pp.gac.cashback_android.ui.RetailerActivity
import ua.pp.gac.cashback_android.ui.ScannerActivity

class MyApplication : Application() {
	override fun onCreate() {
		super.onCreate()

		val scannerIcon = Icon.createWithAdaptiveBitmap(
			createIconWithBackground(R.drawable.ic_barcode, R.color.colorPrimary)
		)
		val productsIcon = Icon.createWithAdaptiveBitmap(
			createIconWithBackground(
				R.drawable.ic_search,
				R.color.colorPrimary
			)
		)
		val retailersIcon = Icon.createWithAdaptiveBitmap(
			createIconWithBackground(R.drawable.ic_retailer, R.color.colorPrimary)
		)

		val shortcutManager = getSystemService(ShortcutManager::class.java)
		val scannerShortcut = android.content.pm.ShortcutInfo.Builder(this, "shortcut_scanner")
			.setShortLabel(getString(R.string.shortcut_scanner_label))
			.setLongLabel(getString(R.string.shortcut_scanner_label_long))
			.setIcon(scannerIcon)
			.setIntent(Intent(this, ScannerActivity::class.java).setAction(Intent.ACTION_VIEW))
			.build()

		val productShortcut = android.content.pm.ShortcutInfo.Builder(this, "shortcut_products")
			.setShortLabel(getString(R.string.shortcut_products_label))
			.setLongLabel(getString(R.string.shortcut_products_label_long))
			.setIcon(productsIcon)
			.setIntent(Intent(this, ProductActivity::class.java).setAction(Intent.ACTION_VIEW))
			.build()

		val retailerShortcut = android.content.pm.ShortcutInfo.Builder(this, "shortcut_retailers")
			.setShortLabel(getString(R.string.shortcut_retailers_label))
			.setLongLabel(getString(R.string.shortcut_retailers_label_long))
			.setIcon(retailersIcon)
			.setIntent(Intent(this, RetailerActivity::class.java).setAction(Intent.ACTION_VIEW))
			.build()

		shortcutManager.dynamicShortcuts =
			listOf(scannerShortcut, productShortcut, retailerShortcut)
		shortcutManager.reportShortcutUsed("shortcut_scanner")
		shortcutManager.reportShortcutUsed("shortcut_products")
		shortcutManager.reportShortcutUsed("shortcut_retailers")

		FirebaseApp.initializeApp(this)
	}

	fun createIconWithBackground(iconRes: Int, backgroundRes: Int): Bitmap {
		val backgroundColor = ContextCompat.getColor(this, backgroundRes)
		val background = ColorDrawable(backgroundColor)
		val icon = ContextCompat.getDrawable(this, iconRes)!!

		val layerDrawable = LayerDrawable(arrayOf(background, icon))
		layerDrawable.setLayerInset(1, 25, 25, 25, 25)

		val bitmap = Bitmap.createBitmap(
			layerDrawable.intrinsicWidth,
			layerDrawable.intrinsicHeight,
			Bitmap.Config.ARGB_8888
		)

		val canvas = Canvas(bitmap)
		layerDrawable.setBounds(0, 0, canvas.width, canvas.height)
		layerDrawable.draw(canvas)

		return bitmap
	}
}
