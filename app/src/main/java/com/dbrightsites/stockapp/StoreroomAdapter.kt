package com.dbrightsites.stockapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class StoreroomAdapter(
    private var items: List<StoreroomItem>,
    private val onEditClick: (StoreroomItem) -> Unit,
    private val onDeleteClick: (StoreroomItem) -> Unit
) : RecyclerView.Adapter<StoreroomAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateChip: Chip = itemView.findViewById(R.id.dateChip)
        val locationChip: Chip = itemView.findViewById(R.id.locationChip)
        val crateNumberText: TextView = itemView.findViewById(R.id.crateNumberText)
        val crateNameText: TextView = itemView.findViewById(R.id.crateNameText)
        val itemNameText: TextView = itemView.findViewById(R.id.itemNameText)
        val itemCountText: TextView = itemView.findViewById(R.id.itemCountText)
        val notesText: TextView = itemView.findViewById(R.id.notesText)
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_storeroom, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.dateChip.text = "📅 ${item.date}"
        holder.locationChip.text = "📍 ${item.location.ifEmpty { "No location" }}"
        holder.crateNumberText.text = item.crateNumber
        holder.crateNameText.text = item.crateName.ifEmpty { "Uncategorized" }
        holder.itemNameText.text = item.itemName
        holder.itemCountText.text = "${item.itemCount} units"
        holder.notesText.text = item.notes.ifEmpty { "No notes added" }

        holder.editButton.setOnClickListener { onEditClick(item) }
        holder.deleteButton.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<StoreroomItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}