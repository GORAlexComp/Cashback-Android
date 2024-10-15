package ua.pp.gac.cashback_android

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.smarteist.autoimageslider.SliderViewAdapter

class MainSliderAdapter(private val imageList: List<Drawable>) :
	SliderViewAdapter<MainSliderAdapter.SliderViewHolder>() {

	inner class SliderViewHolder(itemView: View) : ViewHolder(itemView) {
		val sliderImage: ImageView = itemView.findViewById(R.id.slider_image)
	}

	override fun onCreateViewHolder(parent: ViewGroup): SliderViewHolder {
		val view =
			LayoutInflater.from(parent.context).inflate(R.layout.item_main_slider, parent, false)
		return SliderViewHolder(view)
	}

	override fun onBindViewHolder(viewHolder: SliderViewHolder, position: Int) {
		Glide.with(viewHolder.sliderImage.context).load(imageList[position % imageList.size])
			.into(viewHolder.sliderImage)

	}

	override fun getCount(): Int = imageList.size
}
