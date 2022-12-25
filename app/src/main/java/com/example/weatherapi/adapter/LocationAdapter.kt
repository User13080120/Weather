package com.example.weatherapi.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapi.R
import com.example.weatherapi.api.models.weatherconditions.WeatherCondition
import com.example.weatherapi.utils.Utility
import java.util.*

class LocationAdapter(
    private val mItemClickListener: ItemClickListener,
    private var weatherConditions: MutableList<WeatherCondition>,
) : RecyclerView.Adapter<LocationAdapter.DataViewHolder>(), Filterable {

    private val TAG = "LocationAdapter"

    var weatherConditionFilterList = mutableListOf<WeatherCondition>()

    init {
        weatherConditionFilterList = weatherConditions
    }

    fun getItemData(position: Int): WeatherCondition {
        return weatherConditionFilterList[position]
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DataViewHolder {
        val itemView =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.weather_item, viewGroup, false)
        return DataViewHolder(itemView, mItemClickListener)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        val temp = weatherConditionFilterList[position].currentConditions?.temp.toString()
        val conditions =
            weatherConditionFilterList[position].currentConditions?.conditions.toString()
        val unitGroup = weatherConditionFilterList[position].unitGroup
        val symbol = if (unitGroup == "metric") "C" else "F"

        val text = "$temp°$symbol, $conditions"

        holder.temperatureDescriptionTv.text = text

        holder.locationTv.text = weatherConditionFilterList[position].address

        holder.weatherIv.setImageDrawable(weatherConditionFilterList[position].currentConditions?.let { Utility.getImageIcon(it.icon) })

    }

    override fun getItemCount(): Int {
        return weatherConditionFilterList.size
    }

    class DataViewHolder(view: View, itemClickListener: ItemClickListener?) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        private val TAG = "LocationAdapter"

        var weatherIv: ImageView = view.findViewById(R.id.weather_iv)
        var temperatureDescriptionTv: TextView = view.findViewById(R.id.temperature_description_tv)
        var locationTv: TextView = view.findViewById(R.id.location_tv)

        private var mItemClickListener: ItemClickListener? = itemClickListener

        override fun onClick(view: View) {
            mItemClickListener?.onLocationAdapterItemClick(adapterPosition)

            if (mItemClickListener == null) {
                Log.e(TAG, "mItemClickListener is null!")
            }
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    interface ItemClickListener {
        fun onLocationAdapterItemClick(position: Int)
    }


    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                weatherConditionFilterList = if (charSearch == "") {
                    weatherConditions
                } else {
                    val resultList = mutableListOf<WeatherCondition>()
                    for (row in weatherConditions) {
                        if (row.address.lowercase(Locale.ROOT).contains(
                                constraint.toString()
                                    .lowercase(Locale.ROOT)
                            )
                        ) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = weatherConditionFilterList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results is MutableList<*>) {
                    weatherConditionFilterList =
                        results.filterIsInstance<WeatherCondition>().toMutableList()
                }
                notifyDataSetChanged()
            }
        }
    }

}