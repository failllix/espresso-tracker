package com.thewildzoo.espressotracker.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.thewildzoo.espressotracker.R
import com.thewildzoo.espressotracker.helper.BrewHistoryRecyclerViewAdapterInterface
import com.thewildzoo.espressotracker.model.BrewRecipe
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.* // ktlint-disable no-wildcard-imports
import java.util.concurrent.TimeUnit

class BrewHistoryAdapter(
    private var items: MutableList<BrewRecipe>,
    val context: Context,
    private val brewHistoryRecyclerViewAdapterInterface: BrewHistoryRecyclerViewAdapterInterface
) :
    RecyclerView.Adapter<BrewHistoryAdapter.BrewHistoryViewHolder>() {

    private val decimalFormat: DecimalFormat = DecimalFormat("00").apply {
        roundingMode = RoundingMode.CEILING
        val symbols = DecimalFormatSymbols(Locale.getDefault())
        symbols.decimalSeparator = '.'
        decimalFormatSymbols = symbols
    }

    class BrewHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrewHistoryViewHolder {
        return BrewHistoryViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_brew_recipe_history_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BrewHistoryViewHolder, position: Int) {
        val item = items[position]

        holder.itemView.findViewById<TextView>(R.id.label_coffee_in).apply {
            text = item.coffeeIn.toString()
        }

        holder.itemView.findViewById<TextView>(R.id.label_coffee_out).apply {
            text = item.coffeeOut.toString()
        }

        holder.itemView.findViewById<TextView>(R.id.label_duration).apply {

            val seconds = TimeUnit.MILLISECONDS.toSeconds(item.durationInMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(item.durationInMillis)

            text = if (minutes > 0) {
                decimalFormat.format(minutes) + ":" + decimalFormat.format(seconds) + "." + decimalFormat.format(
                    (item.durationInMillis % 1000) / 10
                )
            } else {
                decimalFormat.format(seconds) + "." + decimalFormat.format(
                    (item.durationInMillis % 1000) / 10
                )
            }
        }

        holder.itemView.findViewById<TextView>(R.id.label_brew_ratio_planned).apply {
            text = resources.getString(
                R.string.label_brew_ratio_number,
                item.plannedBrewRatio.toString()
            )
        }

        holder.itemView.findViewById<TextView>(R.id.label_brew_ratio_actual).apply {
            text = resources.getString(
                R.string.label_brew_ratio_number,
                item.actualBrewRatio.toString()
            )
        }

        holder.itemView.findViewById<MaterialCardView>(R.id.brew_history_card_view).apply {
            setOnLongClickListener {
                brewHistoryRecyclerViewAdapterInterface.deleteItem(position)

                true
            }
        }

        holder.itemView.findViewById<TextView>(R.id.date_label).apply {
            val sdf = SimpleDateFormat("dd.MM.yyyy")

            if (position == 0) {

                item.createdAt?.let { createDate ->
                    val createdAtDate = Date(createDate)
                    val createdAtCalendar = Calendar.getInstance()
                    createdAtCalendar.time = createdAtDate
                    createdAtCalendar.set(Calendar.HOUR_OF_DAY, 0)
                    createdAtCalendar.set(Calendar.MINUTE, 0)
                    createdAtCalendar.set(Calendar.SECOND, 0)
                    createdAtCalendar.set(Calendar.MILLISECOND, 0)
                    text = sdf.format(createdAtDate)
                    this.visibility = View.VISIBLE
                }

            } else if (position > 0 && position < items.size - 1) {
                item.createdAt?.let { createDate ->
                    items[position - 1].createdAt?.let { priorCreateDate ->

                        val createdAtDate = Date(createDate)
                        val createdAtCalendar = Calendar.getInstance()
                        createdAtCalendar.time = createdAtDate
                        createdAtCalendar.set(Calendar.HOUR_OF_DAY, 0)
                        createdAtCalendar.set(Calendar.MINUTE, 0)
                        createdAtCalendar.set(Calendar.SECOND, 0)
                        createdAtCalendar.set(Calendar.MILLISECOND, 0)

                        val nextCreatedAtDate = Date(priorCreateDate)
                        val nextCreatedAtCalendar = Calendar.getInstance()
                        nextCreatedAtCalendar.time = nextCreatedAtDate
                        nextCreatedAtCalendar.set(Calendar.HOUR_OF_DAY, 0)
                        nextCreatedAtCalendar.set(Calendar.MINUTE, 0)
                        nextCreatedAtCalendar.set(Calendar.SECOND, 0)
                        nextCreatedAtCalendar.set(Calendar.MILLISECOND, 0)

                        text = sdf.format(createdAtDate)

                        if (createdAtCalendar.timeInMillis < nextCreatedAtCalendar.timeInMillis) {
                            this.visibility = View.VISIBLE
                        } else {
                            this.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setData(newItems: MutableList<BrewRecipe>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun removeAt(position: Int): BrewRecipe {
        val removed = items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(0, items.size)
        return removed
    }

    fun add(position: Int, recipe: BrewRecipe) {
        val removed = items.add(position, recipe)
        notifyItemInserted(position)
        notifyItemRangeChanged(0, items.size)
        brewHistoryRecyclerViewAdapterInterface.addItem(recipe)
    }
}
