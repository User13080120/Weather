package com.example.weatherapi.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapi.R
import com.example.weatherapi.api.models.weatherconditions.Day
import com.example.weatherapi.utils.Utility

class DayAdapter(
    private val mItemClickListener: ItemClickListener,
    private var days: MutableList<Day>,
    private var unitGroup: String
) : RecyclerView.Adapter<DayAdapter.DataViewHolder>() {

    private val TAG = "DayAdapter"

    fun getItemData(position: Int): Day {
        return days[position]
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DataViewHolder {
        val itemView =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.day_item, viewGroup, false)
        return DataViewHolder(itemView, mItemClickListener)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.dateTv.text = days[position].datetime

        val symbol = if (unitGroup == "metric") "C" else "F"

        holder.tempTv.text = "${days[position].temp}°$symbol"

        holder.weatherIv.setImageDrawable(days[position].let { Utility.getImageIcon(it.icon) })

        try {
            if(days[position].preciptype != null)
            {
                for(precip in days[position].preciptype!!)
                {
                    when(precip){
                        "rain" -> holder.precipTypeRainIv.visibility = View.VISIBLE
                        "snow" -> holder.precipTypeSnowIv.visibility = View.VISIBLE
                        "freezingrain" -> holder.precipTypeFreezingRainIv.visibility = View.VISIBLE
                        "ice" -> holder.precipTypeIceIv.visibility = View.VISIBLE
                    }
                }
            }
        }catch (e : Exception)
        {
            Log.e(TAG, "preciptype was null!")
        }


    }

    override fun getItemCount(): Int {
        return days.size
    }

    class DataViewHolder(view: View, itemClickListener: ItemClickListener?) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        private val TAG = "DayAdapter"

        val dateTv: TextView = view.findViewById(R.id.date_tv)
        val weatherIv: ImageView = view.findViewById(R.id.weather_iv)
        val tempTv: TextView = view.findViewById(R.id.temp_tv)

        val precipTypeRainIv: ImageView = view.findViewById(R.id.precip_type_rain_iv)
        val precipTypeSnowIv: ImageView = view.findViewById(R.id.precip_type_snow_iv)
        val precipTypeFreezingRainIv: ImageView = view.findViewById(R.id.precip_type_freezingrain_iv)
        val precipTypeIceIv: ImageView = view.findViewById(R.id.precip_type_ice_iv)

        private var mItemClickListener: ItemClickListener? = itemClickListener

        override fun onClick(view: View) {
            mItemClickListener?.onDayItemClick(adapterPosition)

            if (mItemClickListener == null) {
                Log.e(TAG, "mItemClickListener is null!")
            }
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    interface ItemClickListener {
        fun onDayItemClick(position: Int)
    }
}