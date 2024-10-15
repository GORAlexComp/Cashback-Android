package ua.pp.gac.cashback_android

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ua.pp.gac.cashback_android.database.RetailersTable.Retailer

class SearchRetailerAdapter(private val retailers: MutableList<Retailer>) :
	RecyclerView.Adapter<SearchRetailerAdapter.RetailerViewHolder>() {
	class RetailerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val brand: TextView = itemView.findViewById(R.id.tv_brand)
		val address: TextView = itemView.findViewById(R.id.tv_address)

		fun bind(retailer: Retailer) {
			brand.text = if (retailer.brand != null) retailer.brand else retailer.manufacture
			address.text = retailer.address
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RetailerViewHolder {
		val view =
			LayoutInflater.from(parent.context)
				.inflate(R.layout.item_retailer_search, parent, false)
		return RetailerViewHolder(view)
	}

	override fun onBindViewHolder(holder: RetailerViewHolder, position: Int) {
		val retailer = retailers[position]
		holder.bind(retailer)
	}

	override fun getItemCount(): Int {
		return retailers.size
	}

	fun addRetailers(newRetailers: List<Retailer>) {
		val startPosition = retailers.size
		retailers.addAll(newRetailers)
		notifyItemRangeInserted(startPosition, newRetailers.size)
	}

	@SuppressLint("NotifyDataSetChanged")
	fun clearRetailers() {
		val size = retailers.size
		retailers.clear()
		notifyItemRangeRemoved(0, size)
	}
}