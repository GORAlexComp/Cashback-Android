package ua.pp.gac.cashback_android

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ua.pp.gac.cashback_android.database.ProductsTable.Product

class SearchProductAdapter(private val products: MutableList<Product>) :
	RecyclerView.Adapter<SearchProductAdapter.ProductViewHolder>() {
	class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val name: TextView = itemView.findViewById(R.id.tv_name)
		val brand: TextView = itemView.findViewById(R.id.tv_brand)
		val ean: TextView = itemView.findViewById(R.id.tv_ean)

		fun bind(product: Product) {
			name.text = product.name
			brand.text = if (product.brand != null) product.brand else product.manufacture
			ean.text = product.ean.toString()
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
		val view =
			LayoutInflater.from(parent.context).inflate(R.layout.item_product_search, parent, false)
		return ProductViewHolder(view)
	}

	override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
		val product = products[position]
		holder.bind(product)
	}

	override fun getItemCount(): Int {
		return products.size
	}

	fun addProducts(newProducts: List<Product>) {
		val startPosition = products.size
		products.addAll(newProducts)
		notifyItemRangeInserted(startPosition, newProducts.size)
	}

	@SuppressLint("NotifyDataSetChanged")
	fun clearProducts() {
		val size = products.size
		products.clear()
		notifyItemRangeRemoved(0, size)
	}
}