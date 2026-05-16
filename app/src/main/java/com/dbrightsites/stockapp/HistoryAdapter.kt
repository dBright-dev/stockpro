package com.dbrightsites.stockapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class HistoryAdapter(
    private var items: List<ItemHistory>
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val actionChip: Chip = itemView.findViewById(R.id.actionChip)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val summaryText: TextView = itemView.findViewById(R.id.summaryText)
        val changesContainer: LinearLayout = itemView.findViewById(R.id.changesContainer)
        val changesText: TextView = itemView.findViewById(R.id.changesText)
        val itemIdText: TextView = itemView.findViewById(R.id.itemIdText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = items[position]

        // Set action chip color and text
        holder.actionChip.text = history.action
        when (history.action) {
            "ADDED" -> {
                holder.actionChip.setChipBackgroundColorResource(android.R.color.holo_green_light)
                holder.actionChip.chipIcon = null
                holder.changesContainer.visibility = View.GONE
            }
            "UPDATED" -> {
                holder.actionChip.setChipBackgroundColorResource(android.R.color.holo_orange_light)
                holder.actionChip.chipIcon = null
                holder.changesContainer.visibility = View.VISIBLE
                holder.changesText.text = buildChangeList(history)
            }
            "DELETED" -> {
                holder.actionChip.setChipBackgroundColorResource(android.R.color.holo_red_light)
                holder.actionChip.chipIcon = null
                holder.changesContainer.visibility = View.VISIBLE
                holder.changesText.text = buildDeletedInfo(history)
            }
        }

        holder.dateText.text = history.dateFormatted
        holder.summaryText.text = history.changeSummary
        holder.itemIdText.text = "Item ID: ${history.itemId.take(8)}..."
    }

    private fun buildChangeList(history: ItemHistory): String {
        val changes = mutableListOf<String>()

        if (history.previousCrateNumber != history.newCrateNumber && history.newCrateNumber.isNotEmpty()) {
            changes.add("• Crate Number: ${history.previousCrateNumber} → ${history.newCrateNumber}")
        }
        if (history.previousCrateName != history.newCrateName && history.newCrateName.isNotEmpty()) {
            changes.add("• Category: ${history.previousCrateName} → ${history.newCrateName}")
        }
        if (history.previousItemName != history.newItemName && history.newItemName.isNotEmpty()) {
            changes.add("• Item: ${history.previousItemName} → ${history.newItemName}")
        }
        if (history.previousItemCount != history.newItemCount && history.newItemCount > 0) {
            changes.add("• Quantity: ${history.previousItemCount} → ${history.newItemCount}")
        }
        if (history.previousLocation != history.newLocation && history.newLocation.isNotEmpty()) {
            changes.add("• Location: ${history.previousLocation} → ${history.newLocation}")
        }
        if (history.previousNotes != history.newNotes && history.newNotes.isNotEmpty()) {
            changes.add("• Notes: ${history.previousNotes.take(20)} → ${history.newNotes.take(20)}")
        }
        if (history.previousDateChecked != history.newDateChecked && history.newDateChecked.isNotEmpty()) {
            changes.add("• Date Checked: ${history.previousDateChecked} → ${history.newDateChecked}")
        }

        return if (changes.isEmpty()) "No detailed changes recorded" else changes.joinToString("\n")
    }

    private fun buildDeletedInfo(history: ItemHistory): String {
        return "Deleted Item:\n" +
                "• Crate: ${history.previousCrateNumber} - ${history.previousCrateName}\n" +
                "• Item: ${history.previousItemName}\n" +
                "• Quantity: ${history.previousItemCount}\n" +
                "• Location: ${history.previousLocation}"
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<ItemHistory>) {
        items = newItems
        notifyDataSetChanged()
    }
}