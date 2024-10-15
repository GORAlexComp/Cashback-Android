package ua.pp.gac.cashback_android.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.pp.gac.cashback_android.R
import ua.pp.gac.cashback_android.SearchRetailerAdapter
import ua.pp.gac.cashback_android.database.DatabaseHelper
import ua.pp.gac.cashback_android.database.RetailersTable

class RetailerActivity : AppCompatActivity() {
	private lateinit var dbHelper: DatabaseHelper
	private lateinit var searchRetailerAdapter: SearchRetailerAdapter
	private lateinit var retailersListView: RecyclerView
	private var currentQueryBrand: String = ""
	private var currentQueryAddress: String = ""
	private var isLoading = false
	private var currentPage = 0
	private val limit = 25

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_retailer)

		val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
		toolbarTitle.text = getString(R.string.retailers)

		val btnBack = findViewById<ImageButton>(R.id.back)
		btnBack.setOnClickListener {
			onBackPressedDispatcher.onBackPressed()
		}

		dbHelper = DatabaseHelper(this)

		retailersListView = findViewById(R.id.retailers_list)
		retailersListView.layoutManager = LinearLayoutManager(this)

		searchRetailerAdapter = SearchRetailerAdapter(mutableListOf())
		retailersListView.adapter = searchRetailerAdapter

		loadMoreRetailers(currentQueryBrand, currentQueryAddress, currentPage)

		val etBrand: EditText = findViewById(R.id.et_brand)
		val etAddress: EditText = findViewById(R.id.et_address)

		val textWatcher = object : TextWatcher {
			override fun afterTextChanged(s: Editable?) {
				currentQueryBrand = etBrand.text.toString()
				currentQueryAddress = etAddress.text.toString()
				currentPage = 0
				searchRetailerAdapter.clearRetailers()
				loadMoreRetailers(currentQueryBrand, currentQueryAddress, currentPage)
			}

			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
		}
		etBrand.addTextChangedListener(textWatcher)
		etAddress.addTextChangedListener(textWatcher)

		retailersListView.addOnScrollListener(
			object : RecyclerView.OnScrollListener() {
				override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
					super.onScrolled(recyclerView, dx, dy)

					val layoutManager = recyclerView.layoutManager as LinearLayoutManager
					val visibleItemCount = layoutManager.childCount
					val totalItemCount = layoutManager.itemCount
					val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

					if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
						loadMoreRetailers(currentQueryBrand, currentQueryAddress, currentPage)
					}
				}
			}
		)
	}

	@OptIn(DelicateCoroutinesApi::class)
	private fun loadMoreRetailers(queryBrand: String, queryAddress: String, page: Int) {
		isLoading = true

		GlobalScope.launch(Dispatchers.IO) {
			val offset = page * limit
			val retailers =
				RetailersTable.getSearchRetailers(
					dbHelper.readableDatabase,
					queryBrand,
					queryAddress,
					offset,
					limit
				)

			withContext(Dispatchers.Main) {
				searchRetailerAdapter.addRetailers(retailers)
				currentPage++
				isLoading = false
			}
		}
	}
}
