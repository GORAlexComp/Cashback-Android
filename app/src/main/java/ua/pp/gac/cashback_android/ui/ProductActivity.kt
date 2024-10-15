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
import ua.pp.gac.cashback_android.SearchProductAdapter
import ua.pp.gac.cashback_android.database.DatabaseHelper
import ua.pp.gac.cashback_android.database.ProductsTable

class ProductActivity : AppCompatActivity() {
	private lateinit var dbHelper: DatabaseHelper
	private lateinit var searchProductAdapter: SearchProductAdapter
	private lateinit var productsListView: RecyclerView
	private var currentQuery: String = ""
	private var isLoading = false
	private var currentPage = 0
	private val limit = 25

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_product)

		val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
		toolbarTitle.text = getString(R.string.search_products)

		val btnBack = findViewById<ImageButton>(R.id.back)
		btnBack.setOnClickListener {
			onBackPressedDispatcher.onBackPressed()
		}

		dbHelper = DatabaseHelper(this)

		productsListView = findViewById(R.id.products_list)
		productsListView.layoutManager = LinearLayoutManager(this)

		searchProductAdapter = SearchProductAdapter(mutableListOf())
		productsListView.adapter = searchProductAdapter

		loadMoreProducts(currentQuery, currentPage)

		val etNameEan: EditText = findViewById(R.id.et_name_ean)
		etNameEan.addTextChangedListener(object : TextWatcher {
			override fun afterTextChanged(s: Editable?) {
				currentQuery = s.toString()
				currentPage = 0
				searchProductAdapter.clearProducts()
				loadMoreProducts(currentQuery, currentPage)
			}

			override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
			override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
		})

		productsListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				super.onScrolled(recyclerView, dx, dy)

				val layoutManager = recyclerView.layoutManager as LinearLayoutManager
				val visibleItemCount = layoutManager.childCount
				val totalItemCount = layoutManager.itemCount
				val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

				if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
					loadMoreProducts(currentQuery, currentPage)
				}
			}
		})
	}

	@OptIn(DelicateCoroutinesApi::class)
	private fun loadMoreProducts(query: String, page: Int) {
		isLoading = true

		GlobalScope.launch(Dispatchers.IO) {
			val offset = page * limit
			val products =
				ProductsTable.getSearchProducts(dbHelper.readableDatabase, query, offset, limit)

			withContext(Dispatchers.Main) {
				searchProductAdapter.addProducts(products)
				currentPage++
				isLoading = false
			}
		}
	}
}
