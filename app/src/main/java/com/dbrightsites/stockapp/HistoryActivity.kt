package com.dbrightsites.stockapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryActivity : AppCompatActivity() {

    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var fabBack: FloatingActionButton
    private lateinit var totalChangesText: TextView
    private lateinit var clearHistoryButton: Button
    private lateinit var filterAll: Button
    private lateinit var filterAdded: Button
    private lateinit var filterUpdated: Button
    private lateinit var filterDeleted: Button

    private lateinit var database: DatabaseReference
    private lateinit var adapter: HistoryAdapter
    private val allHistoryList = mutableListOf<ItemHistory>()
    private val filteredHistoryList = mutableListOf<ItemHistory>()
    private var currentFilter = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        // Initialize views
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        fabBack = findViewById(R.id.fabBack)
        totalChangesText = findViewById(R.id.totalChangesText)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        filterAll = findViewById(R.id.filterAll)
        filterAdded = findViewById(R.id.filterAdded)
        filterUpdated = findViewById(R.id.filterUpdated)
        filterDeleted = findViewById(R.id.filterDeleted)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().getReference("item_history")

        setupRecyclerView()
        loadHistoryFromFirebase()
        setupFilters()

        // Back button
        fabBack.setOnClickListener {
            finish()
        }

        // Clear history button
        clearHistoryButton.setOnClickListener {
            showClearHistoryDialog()
        }
    }

    private fun updateFilterButtonStates(selectedButton: Button) {
        val buttons = listOf(filterAll, filterAdded, filterUpdated, filterDeleted)
        buttons.forEach { button ->
            button.isEnabled = button != selectedButton
        }
    }
    private fun setupFilters() {
        filterAll.setOnClickListener {
            currentFilter = "ALL"
            applyFilter()
            updateFilterButtonStates(filterAll)
        }

        filterAdded.setOnClickListener {
            currentFilter = "ADDED"
            applyFilter()
            updateFilterButtonStates(filterAdded)
        }

        filterUpdated.setOnClickListener {
            currentFilter = "UPDATED"
            applyFilter()
            updateFilterButtonStates(filterUpdated)
        }

        filterDeleted.setOnClickListener {
            currentFilter = "DELETED"
            applyFilter()
            updateFilterButtonStates(filterDeleted)
        }
    }


    private fun applyFilter() {
        filteredHistoryList.clear()

        for (history in allHistoryList) {
            when (currentFilter) {
                "ALL" -> filteredHistoryList.add(history)
                "ADDED" -> if (history.action == "ADDED") filteredHistoryList.add(history)
                "UPDATED" -> if (history.action == "UPDATED") filteredHistoryList.add(history)
                "DELETED" -> if (history.action == "DELETED") filteredHistoryList.add(history)
            }
        }

        filteredHistoryList.sortByDescending { it.timestamp }
        adapter.updateItems(filteredHistoryList)
        totalChangesText.text = "Showing: ${filteredHistoryList.size} of ${allHistoryList.size} changes"
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(filteredHistoryList)
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = adapter
    }

    private fun loadHistoryFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allHistoryList.clear()
                for (itemSnapshot in snapshot.children) {
                    val history = itemSnapshot.getValue(ItemHistory::class.java)
                    if (history != null) {
                        history.id = itemSnapshot.key ?: ""
                        allHistoryList.add(history)
                    }
                }
                allHistoryList.sortByDescending { it.timestamp }
                applyFilter()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HistoryActivity, "Failed to load history: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showClearHistoryDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear History")
            .setMessage("Are you sure you want to clear all history? This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                database.removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to clear: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}