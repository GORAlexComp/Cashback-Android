package ua.pp.gac.cashback_android.ui

import android.graphics.Canvas
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.DelicateCoroutinesApi
import ua.pp.gac.cashback_android.FavoriteProductAdapter
import ua.pp.gac.cashback_android.R
import ua.pp.gac.cashback_android.database.DatabaseHelper
import ua.pp.gac.cashback_android.database.FavoritesTable

class FavoriteActivity : AppCompatActivity() {
	private lateinit var dbHelper: DatabaseHelper
	private lateinit var favoriteProductAdapter: FavoriteProductAdapter
	private lateinit var favoritesList: RecyclerView
	private lateinit var tvFavoriteCount: TextView
	private var isLoading = false
	private var currentPage = 0
	private val limit = 25

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_favorite)

		val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
		toolbarTitle.text = getString(R.string.favorites)

		val btnBack = findViewById<ImageButton>(R.id.back)
		btnBack.setOnClickListener {
			onBackPressedDispatcher.onBackPressed()
		}

		dbHelper = DatabaseHelper(this)

		tvFavoriteCount = findViewById<TextView>(R.id.favorite_count)
		tvFavoriteCount.text =
			FavoritesTable.getFavoritesCount(dbHelper.readableDatabase).toString()

		favoritesList = findViewById<RecyclerView>(R.id.favorite_list)
		favoritesList.layoutManager = LinearLayoutManager(this)

		favoriteProductAdapter = FavoriteProductAdapter(this, mutableListOf())
		favoritesList.adapter = favoriteProductAdapter

		loadMoreFavorites(currentPage)

		favoritesList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				super.onScrolled(recyclerView, dx, dy)

				val layoutManager = recyclerView.layoutManager as LinearLayoutManager
				val visibleItemCount = layoutManager.childCount
				val totalItemCount = layoutManager.itemCount
				val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

				if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
					loadMoreFavorites(currentPage)
				}
			}
		})

		val itemTouchHelper = ItemTouchHelper(object :
			ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
			override fun onMove(
				recyclerView: RecyclerView,
				viewHolder: RecyclerView.ViewHolder,
				target: RecyclerView.ViewHolder,
			): Boolean {
				return false
			}

			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				val position = viewHolder.adapterPosition
				val favorite = favoriteProductAdapter.getItem(position)

				FavoritesTable.delFavorite(dbHelper.writableDatabase, favorite.ean)

				favoriteProductAdapter.removeAt(position)
				tvFavoriteCount.text =
					(Integer.valueOf(tvFavoriteCount.text.toString()) - 1).toString()

				Toast.makeText(
					this@FavoriteActivity,
					"Продукт видалено з вибраного",
					Toast.LENGTH_SHORT
				).show()
			}

			override fun onChildDraw(
				c: Canvas,
				recyclerView: RecyclerView,
				viewHolder: RecyclerView.ViewHolder,
				dX: Float,
				dY: Float,
				actionState: Int,
				isCurrentlyActive: Boolean,
			) {
				super.onChildDraw(
					c,
					recyclerView,
					viewHolder,
					dX,
					dY,
					actionState,
					isCurrentlyActive
				)

				val itemView = viewHolder.itemView
				val icon = ContextCompat.getDrawable(this@FavoriteActivity, R.drawable.ic_trash)
				icon?.setTint(getColor(R.color.checkCancel))
				val intrinsicWidth = icon?.intrinsicWidth ?: 0
				val intrinsicHeight = icon?.intrinsicHeight ?: 0
				val iconMargin = (itemView.height - intrinsicHeight) / 2
				val iconTop = itemView.top + (itemView.height - intrinsicHeight) / 2
				val iconBottom = iconTop + intrinsicHeight

				if (dX > 0) {
					val iconLeft = itemView.left + iconMargin
					val iconRight = iconLeft + intrinsicWidth
					icon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
					icon?.draw(c)
				} else if (dX < 0) {
					val iconLeft = itemView.right - iconMargin - intrinsicWidth
					val iconRight = itemView.right - iconMargin
					icon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
					icon?.draw(c)
				}
			}
		})

		itemTouchHelper.attachToRecyclerView(favoritesList)
	}

	@OptIn(DelicateCoroutinesApi::class)
	private fun loadMoreFavorites(page: Int) {
		isLoading = true

		val offset = page * limit
		val favorites = FavoritesTable.getFavorites(dbHelper.readableDatabase, offset, limit)

		favoriteProductAdapter.addFavorites(favorites)
		currentPage++
		isLoading = false
	}
}
