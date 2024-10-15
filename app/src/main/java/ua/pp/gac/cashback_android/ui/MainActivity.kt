package ua.pp.gac.cashback_android.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.smarteist.autoimageslider.SliderView
import ua.pp.gac.cashback_android.MainSliderAdapter
import ua.pp.gac.cashback_android.MoreFunctions
import ua.pp.gac.cashback_android.R
import ua.pp.gac.cashback_android.database.DatabaseHelper
import ua.pp.gac.cashback_android.database.ProductsTable
import ua.pp.gac.cashback_android.database.RetailersTable
import ua.pp.gac.cashback_android.database.SettingsTable
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity() {
	private lateinit var dbHelper: DatabaseHelper
	private lateinit var analytics: FirebaseAnalytics
	private lateinit var sliderView: SliderView
	private lateinit var productsCountText: TextView
	private lateinit var retailersCountText: TextView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		analytics = Firebase.analytics

		val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
		toolbar.title = ""
		setSupportActionBar(toolbar)

		requestPermissions()
		try {
			dbHelper = DatabaseHelper(this)
		} catch (_: Exception) {
			Toast.makeText(this, R.string.error_start_db, Toast.LENGTH_SHORT).show()
		}
		MoreFunctions(this).applyTheme(SettingsTable.getThemeMode(dbHelper.readableDatabase))

		sliderView = findViewById<SliderView>(R.id.slider_view)
		val imagesSlider = loadImagesFromAssets()
		val adapter = MainSliderAdapter(imagesSlider)
		sliderView.setSliderAdapter(adapter)
		sliderView.isAutoCycle = true
		sliderView.startAutoCycle()

		val welcomeMsg: TextView = findViewById(R.id.welcome_msg)
		val openScannerButton: Button = findViewById(R.id.btn_open_scanner)
		val openProductButton: LinearLayout = findViewById(R.id.btn_product)
		val openRulesButton: LinearLayout = findViewById(R.id.btn_rules)
		val openRetailerButton: LinearLayout = findViewById(R.id.btn_retailer)
		val openFavoriteButton: LinearLayout = findViewById(R.id.btn_favorite)
		val openSettingsButton: ImageView = findViewById(R.id.btn_settings)
		val bankPartnersButton: LinearLayout = findViewById(R.id.bank_partners)
		productsCountText = findViewById(R.id.products_count_text)
		retailersCountText = findViewById(R.id.retailers_count_text)

		val calendar = Calendar.getInstance()
		val hour = calendar.get(Calendar.HOUR_OF_DAY)

		val welcome = when (hour) {
			in 0..4 -> getString(R.string.good_night).toString()
			in 5..11 -> getString(R.string.good_morning).toString()
			in 12..17 -> getString(R.string.good_day).toString()
			in 18..23 -> getString(R.string.good_evening).toString()
			else -> getString(R.string.welcome_default).toString()
		}

		welcomeMsg.text = welcome

		updateTablesCount()
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			registerReceiver(
				updateReceiver, IntentFilter("ua.pp.gac.cashback_android.UPDATE_TABLES_COUNT"),
				RECEIVER_NOT_EXPORTED
			)
		}

		openScannerButton.setOnClickListener {
			val intent = Intent(this, ScannerActivity::class.java)
			startActivity(intent)
		}

		openProductButton.setOnClickListener {
			val intent = Intent(this, ProductActivity::class.java)
			startActivity(intent)
		}

		openRulesButton.setOnClickListener {
			val intent = Intent(this, RulesActivity::class.java)
			startActivity(intent)
		}

		openRetailerButton.setOnClickListener {
			val intent = Intent(this, RetailerActivity::class.java)
			startActivity(intent)
		}

		openFavoriteButton.setOnClickListener {
			val intent = Intent(this, FavoriteActivity::class.java)
			startActivity(intent)
		}

		openSettingsButton.setOnClickListener {
			val intent = Intent(this, SettingsActivity::class.java)
			startActivity(intent)
		}

		bankPartnersButton.setOnClickListener {
			val intent = Intent(this, PartnerBankActivity::class.java)
			startActivity(intent)
		}
	}

	@SuppressLint("SetTextI18n")
	private fun updateTablesCount() {
		val productCount = ProductsTable.getProductsCount(dbHelper.readableDatabase)
		productsCountText.text = getString(R.string.table_products_all) + " $productCount"

		val retailerCount = RetailersTable.getRetailersCount(dbHelper.readableDatabase)
		retailersCountText.text = getString(R.string.table_retailers_all) + " $retailerCount"
	}

	private val updateReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			updateTablesCount()
		}
	}

	private fun loadImagesFromAssets(): List<Drawable> {
		val folder = "slider_images"
		val assetManager = assets
		val images = mutableListOf<Drawable>()

		try {
			val files = assetManager.list(folder)
			files?.forEach { fileName ->
				val inputStream: InputStream = assetManager.open("$folder/$fileName")
				val drawable = Drawable.createFromStream(inputStream, null)
				images.add(drawable!!)
				inputStream.close()
			}
		} catch (e: IOException) {
			Toast.makeText(
				this,
				getString(R.string.error_load_slider).toString(),
				Toast.LENGTH_SHORT
			).show()
			e.printStackTrace()
		}

		return images
	}

	@SuppressLint("InlinedApi")
	private fun requestPermissions() {
		val permissions = arrayOf(
			Manifest.permission.CAMERA,
			Manifest.permission.INTERNET,
			Manifest.permission.POST_NOTIFICATIONS
		)

		val permissionsToRequest = permissions.filter {
			ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
		}.toTypedArray()

		if (permissionsToRequest.isNotEmpty()) {
			ActivityCompat.requestPermissions(this, permissionsToRequest, 100)
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		unregisterReceiver(updateReceiver)
	}
}
