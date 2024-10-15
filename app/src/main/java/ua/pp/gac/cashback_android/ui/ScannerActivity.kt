package ua.pp.gac.cashback_android.ui

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import ua.pp.gac.cashback_android.R
import ua.pp.gac.cashback_android.database.DatabaseHelper
import ua.pp.gac.cashback_android.database.FavoritesTable
import ua.pp.gac.cashback_android.database.ProductsTable
import java.util.Hashtable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Suppress("DEPRECATION")
class ScannerActivity : ComponentActivity() {
	private lateinit var dbHelper: DatabaseHelper
	private lateinit var sharedP: SharedPreferences
//	private lateinit var cameraHelper: CameraHelper
	private lateinit var cameraExecutor: ExecutorService
	private lateinit var previewView: PreviewView
	private var camera: Camera? = null
	private var cameraControl: CameraControl? = null
	private var isFlashEnabled: Boolean = false

	private lateinit var scannerStartScreen: LinearLayout
	private lateinit var resultScanner: LinearLayout
	private lateinit var resultScannerOk: LinearLayout
	private lateinit var resultScannerButtons: LinearLayout
	private lateinit var ivScanIcon: ImageView
	private lateinit var tvScanMessage: TextView
	private lateinit var scanAgainButton: MaterialButton
	private lateinit var btnFavorite: MaterialButton
	private lateinit var flashlight: ImageButton

	@ExperimentalGetImage
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_scanner)

		dbHelper = DatabaseHelper(this)

		previewView = findViewById(R.id.preview_view)
		cameraExecutor = Executors.newSingleThreadExecutor()

		scannerStartScreen = findViewById(R.id.scanner_start_screen)
		resultScanner = findViewById(R.id.result_scanner)
		resultScannerOk = findViewById(R.id.result_scanner_ok)
		resultScannerButtons = findViewById(R.id.result_scanner_buttons)
		ivScanIcon = findViewById(R.id.iv_scan_icon)
		tvScanMessage = findViewById(R.id.tv_scan_message)
		scanAgainButton = findViewById(R.id.btn_scan_again)
		btnFavorite = findViewById(R.id.btn_favorite)
		flashlight = findViewById(R.id.flash_button)

		scannerStartScreen.visibility = View.VISIBLE
		resultScanner.visibility = View.GONE
		resultScannerButtons.visibility = View.GONE

		sharedP = getSharedPreferences("camera_prefs", MODE_PRIVATE)
		isFlashEnabled = sharedP.getBoolean("flash_state", false)

		startCamera()

		flashlight.setOnClickListener {
			toggleFlash()
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@ExperimentalGetImage
	private fun startCamera() {
		val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

		cameraProviderFuture.addListener({
			val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

			val preview = Preview.Builder().build().also { previewUseCase ->
				previewUseCase.setSurfaceProvider(previewView.surfaceProvider)
			}

			val imageAnalysis = ImageAnalysis.Builder()
				.setTargetAspectRatio(AspectRatio.RATIO_16_9)
				.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
				.build()
			imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy -> processImage(imageProxy) }

			val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

			try {
				cameraProvider.unbindAll()
				camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
				cameraControl = camera?.cameraControl

				camera?.cameraControl?.enableTorch(isFlashEnabled)
				updateFlashButton(isFlashEnabled)

				previewView.setOnTouchListener { _, event ->
					if (event.action == MotionEvent.ACTION_DOWN) {
						val factory = previewView.meteringPointFactory
						val point = factory.createPoint(event.x, event.y)
						val action = FocusMeteringAction.Builder(point).build()

						cameraControl?.startFocusAndMetering(action)
						return@setOnTouchListener true
					}
					false
				}
			} catch (_: Exception) {
				Toast.makeText(this, R.string.error_start_camera, Toast.LENGTH_SHORT).show()
			}
		}, ContextCompat.getMainExecutor(this))
	}

	@ExperimentalGetImage
	private fun processImage(imageProxy: ImageProxy) {
		val mediaImage = imageProxy.image ?: return
		val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

		val scanner = BarcodeScanning.getClient()
		scanner.process(image)
			.addOnSuccessListener { barcodes ->
				for (barcode in barcodes) {
					val rawValue = barcode.rawValue
					if (rawValue != null) {
						val ean = rawValue.toLongOrNull()
						ean?.let {
							checkBarcodeInDatabase(ean)
						} ?: Toast.makeText(this, R.string.error_ean_format, Toast.LENGTH_SHORT)
							.show()
					}
				}
			}
			.addOnFailureListener { _ ->
				Toast.makeText(this, R.string.error_get_product_data, Toast.LENGTH_SHORT).show()
			}
			.addOnCompleteListener {
				imageProxy.close()
			}
	}

	private fun showBottomSheet(
		product: ProductsTable.Product,
	) {
		try {
			stopScanning()

			scannerStartScreen.visibility = View.GONE
			resultScanner.visibility = View.VISIBLE
			resultScannerOk.visibility = View.VISIBLE
			resultScannerButtons.visibility = View.VISIBLE
			btnFavorite.visibility = View.VISIBLE
			ivScanIcon.setImageResource(R.drawable.ic_check_ok)
			ivScanIcon.contentDescription = getString(R.string.product_find_icon_desc)
			tvScanMessage.setText(R.string.product_find_ok)

			findViewById<TextView>(R.id.tv_name)?.text = product.name
			findViewById<TextView>(R.id.tv_ean)?.text = product.ean.toString()
			findViewById<TextView>(R.id.tv_brand)?.text = product.brand ?: product.manufacture
			findViewById<TextView>(R.id.tv_updated_at)?.text = product.updatedAt

			val barcodeImageView = findViewById<ImageView>(R.id.iv_barcode)
			val barcodeBitmap = generateBarcodeBitmap(product.ean.toString())
			barcodeImageView.setImageBitmap(barcodeBitmap)

			scanAgainButton.setOnClickListener {
				scannerStartScreen.visibility = View.VISIBLE
				resultScanner.visibility = View.GONE
				resultScannerButtons.visibility = View.GONE

				resumeScanning()
			}

			var favorite = FavoritesTable.isFavorite(dbHelper.readableDatabase, product.ean)
			if (favorite) {
				btnFavorite.setIcon(
					ContextCompat.getDrawable(
						this,
						R.drawable.ic_favorite_fill
					)
				)
				btnFavorite.setIconTintResource(R.color.checkCancel)
			} else {
				btnFavorite.setIcon(
					ContextCompat.getDrawable(
						this,
						R.drawable.ic_favorite
					)
				)
				btnFavorite.setIconTintResource(R.color.checkCancel)
			}

			btnFavorite.setOnClickListener {
				if (favorite) {
					val remove = FavoritesTable.delFavorite(dbHelper.writableDatabase, product.ean)
					if (remove) {
						favorite = false
						Toast.makeText(this, R.string.product_del_favorite, Toast.LENGTH_SHORT)
							.show()

						btnFavorite.setIcon(
							ContextCompat.getDrawable(
								this,
								R.drawable.ic_favorite
							)
						)
						btnFavorite.setIconTintResource(R.color.checkCancel)
					} else {
						Toast.makeText(this, R.string.error_del_favorite, Toast.LENGTH_SHORT).show()
					}
				} else {
					val setFavorite = FavoritesTable.setFavorite(dbHelper.writableDatabase, product)
					if (setFavorite) {
						favorite = true
						Toast.makeText(this, R.string.product_set_favorite, Toast.LENGTH_SHORT)
							.show()

						btnFavorite.setIcon(
							ContextCompat.getDrawable(this, R.drawable.ic_favorite_fill)
						)
						btnFavorite.setIconTintResource(R.color.checkCancel)
					} else {
						Toast.makeText(this, R.string.error_set_favorite, Toast.LENGTH_SHORT).show()
					}
				}
			}
		} catch (_: Exception) {
			Toast.makeText(this, R.string.error_get_product_data, Toast.LENGTH_SHORT).show()
		}
	}

	@SuppressLint("InflateParams")
	private fun showNotFoundBottomSheet() {
		try {
			stopScanning()

			scannerStartScreen.visibility = View.GONE
			resultScanner.visibility = View.VISIBLE
			resultScannerOk.visibility = View.GONE
			resultScannerButtons.visibility = View.VISIBLE
			btnFavorite.visibility = View.GONE
			ivScanIcon.setImageResource(R.drawable.ic_check_cancel)
			ivScanIcon.contentDescription = getString(R.string.product_notFind_icon_desc)
			tvScanMessage.setText(R.string.product_find_cancel)

			scanAgainButton.setOnClickListener {
				scannerStartScreen.visibility = View.VISIBLE
				resultScanner.visibility = View.GONE
				resultScannerButtons.visibility = View.GONE

				resumeScanning()
			}
		} catch (_: Exception) {
			Toast.makeText(this, R.string.error_get_product_data, Toast.LENGTH_SHORT).show()
		}
	}

	private fun stopScanning() {
		cameraExecutor.shutdown()
	}

	@OptIn(ExperimentalGetImage::class)
	private fun resumeScanning() {
		cameraExecutor = Executors.newSingleThreadExecutor()
		startCamera()
	}

	private fun toggleFlash() {
		isFlashEnabled = !isFlashEnabled
		camera?.cameraControl?.enableTorch(isFlashEnabled)
		sharedP.edit().putBoolean("flash_state", isFlashEnabled).apply()
		updateFlashButton(isFlashEnabled)
	}

	private fun updateFlashButton(enabled: Boolean) {
		val drawableResId =
			if (enabled) R.drawable.ic_flashlight_off else R.drawable.ic_flashlight_on
		flashlight.setImageResource(drawableResId)
	}

	private fun checkBarcodeInDatabase(ean: Long) {
		val product = ProductsTable.getProduct(dbHelper.readableDatabase, ean)

		if (product != null) {
			showBottomSheet(product)
		} else {
			showNotFoundBottomSheet()
		}
	}

	private fun generateBarcodeBitmap(ean: String): Bitmap {
		val width = 600
		val height = 300
		val barcodeFormat = when (ean.length) {
			13 -> BarcodeFormat.EAN_13
			12 -> BarcodeFormat.UPC_A
			8 -> BarcodeFormat.EAN_8
			else -> BarcodeFormat.CODE_39
		}

		val hints = Hashtable<EncodeHintType, Any>()
		hints[EncodeHintType.MARGIN] = 1

		val bitMatrix = MultiFormatWriter().encode(ean, barcodeFormat, width, height, hints)
		val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

		for (x in 0 until width) {
			for (y in 0 until height) {
				bitmap.setPixel(
					x,
					y,
					if (bitMatrix[x, y]) getColor(R.color.colorPrimary) else Color.TRANSPARENT
				)
			}
		}
		return bitmap
	}

	override fun onDestroy() {
		super.onDestroy()
		cameraExecutor.shutdown()
	}
}
