package ua.pp.gac.cashback_android

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import ua.pp.gac.cashback_android.database.FavoritesTable.Favorite
import java.util.Hashtable

class FavoriteProductAdapter(
	private val context: Context,
	private val favorites: MutableList<Favorite>,
) :
	RecyclerView.Adapter<FavoriteProductAdapter.FavoriteViewHolder>() {
	class FavoriteViewHolder(
		itemView: View,
		private val context: Context,
		private val favorites: MutableList<Favorite>,
	) : RecyclerView.ViewHolder(itemView) {
		val name: TextView = itemView.findViewById(R.id.product_name)
		val brand: TextView = itemView.findViewById(R.id.product_brand)
		val ean: Button = itemView.findViewById(R.id.btn_view_ean)

		fun bind(favorite: Favorite) {
			name.text = favorite.name
			brand.text = favorite.brand

			val barcode = FavoriteProductAdapter(
				context,
				favorites
			).generateBarcodeBitmap(favorite.ean.toString())

			ean.setOnClickListener {
				FavoriteProductAdapter(
					context,
					favorites
				).showEanDialog(favorite.ean, barcode)
			}
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
		val view = LayoutInflater.from(context).inflate(R.layout.item_product_fav, parent, false)
		return FavoriteViewHolder(view, context, favorites)
	}

	override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
		val favorite = favorites[position]
		holder.bind(favorite)
	}

	override fun getItemCount(): Int {
		return favorites.size
	}

	fun getItem(position: Int): Favorite {
		return favorites[position]
	}

	fun addFavorites(newFavorites: List<Favorite>) {
		val startPosition = favorites.size
		favorites.addAll(newFavorites)
		notifyItemRangeInserted(startPosition, newFavorites.size)
	}

	@SuppressLint("InflateParams")
	fun showEanDialog(ean: Long?, imageResId: Bitmap) {
		val dialogBuilder = AlertDialog.Builder(context, R.style.Theme_CashbackAndroid_Dialog)

		val dialogHeader = LayoutInflater.from(context).inflate(R.layout.dialog_header, null)
		dialogHeader.findViewById<TextView>(R.id.toolbar_title).text =
			getString(context, R.string.product_barcode)
		dialogBuilder.setCustomTitle(dialogHeader)

		val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_favorite_code, null)
		val eanTextView: TextView = dialogView.findViewById(R.id.tv_ean)
		val eanImageView: ImageView = dialogView.findViewById(R.id.iv_ean)
		eanTextView.text = ean.toString()
		eanImageView.setImageBitmap(imageResId)
		dialogBuilder.setView(dialogView)

		val dialog = dialogBuilder.create()

		val dialogFooter = dialogView.findViewById<ViewGroup>(R.id.dialog_footer)
		dialogFooter.findViewById<MaterialButton>(R.id.ok_button_dialog).setOnClickListener {
			dialog.dismiss()
		}
		dialog.show()
	}

	fun removeAt(position: Int) {
		favorites.removeAt(position)
		notifyItemRemoved(position)
	}

	fun generateBarcodeBitmap(ean: String): Bitmap {
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
					if (bitMatrix[x, y]) getColor(
						context,
						R.color.colorPrimary
					) else Color.TRANSPARENT
				)
			}
		}
		return bitmap
	}
}