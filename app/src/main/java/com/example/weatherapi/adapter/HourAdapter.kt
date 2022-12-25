package com.example.weatherapi.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapi.R
import com.example.weatherapi.api.models.weatherconditions.Hour
import com.example.weatherapi.utils.Utility

class HourAdapter(
    private val mItemClickListener: ItemClickListener,
    private var hours: MutableList<Hour>,
    private var unitGroup: String,
    val sunriseEpoch: Long,
    val sunsetEpoch: Long
) : RecyclerView.Adapter<HourAdapter.DataViewHolder>() {

    private val TAG = "HourAdapter"

    fun getItemData(position: Int): Hour {
        return hours[position]
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DataViewHolder {
        val itemView =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.hour_item, viewGroup, false)
        return DataViewHolder(itemView, mItemClickListener)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.timeTv.text = hours[position].datetime.take(5)

        val symbol = if (unitGroup == "metric") "C" else "F"

        holder.tempTv.text = "${hours[position].temp}°$symbol"

        try{
            holder.weatherIv.setImageDrawable(hours[position].let { it.datetimeEpoch?.let { it1 ->
                Utility.getImageIcon(it.icon, sunriseEpoch, sunsetEpoch,
                    it1
                )
            } })
        }
        catch (e : Exception)
        {
            Log.e(TAG, "Couldn't set weather image view!")
        }

    }

    override fun getItemCount(): Int {
        return hours.size
    }

    class DataViewHolder(view: View, itemClickListener: ItemClickListener?) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        private val TAG = "HourAdapter"

        val timeTv: TextView = view.findViewById(R.id.time_tv)
        val weatherIv: ImageView = view.findViewById(R.id.weather_iv)
        val tempTv: TextView = view.findViewById(R.id.temp_tv)

        private var mItemClickListener: ItemClickListener? = itemClickListener

        override fun onClick(view: View) {
            mItemClickListener?.onHourItemClick(adapterPosition)

            if (mItemClickListener == null) {
                Log.e(TAG, "mItemClickListener is null!")
            }
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    interface ItemClickListener {
        fun onHourItemClick(position: Int)
    }

}