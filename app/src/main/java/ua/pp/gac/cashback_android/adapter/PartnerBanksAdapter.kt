package ua.pp.gac.cashback_android

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PartnerBanksAdapter(
	private val banksList: List<Bank>,
	private val onItemClick: (Bank) -> Unit,
) : RecyclerView.Adapter<PartnerBanksAdapter.BanksViewHolder>() {
	data class Bank(
		val name: String,
		val iconPath: String,
		val type: String,
		val deep: String,
	)

	inner class BanksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val iconBank: ImageView = itemView.findViewById(R.id.bank_icon)
		val nameBank: TextView = itemView.findViewById(R.id.bank_name)
		val bankItem: View = itemView.findViewById(R.id.bank_item)
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BanksViewHolder {
		val view =
			LayoutInflater.from(parent.context).inflate(R.layout.item_bank_partner, parent, false)
		return BanksViewHolder(view)
	}

	override fun onBindViewHolder(viewHolder: BanksViewHolder, position: Int) {
		val bank = banksList[position]

		Glide.with(viewHolder.iconBank.context)
			.load("file:///android_asset/bank_logos/${bank.iconPath}")
			.into(viewHolder.iconBank)

		viewHolder.nameBank.text = bank.name

		viewHolder.bankItem.setOnClickListener {
			onItemClick(bank)
		}
	}

	override fun getItemCount(): Int = banksList.size
}