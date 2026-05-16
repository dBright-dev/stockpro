package com.dbrightsites.stockapp

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var totalEntriesText: TextView
    private lateinit var totalItemsText: TextView
    private lateinit var uniqueProductsText: TextView
    private lateinit var uniqueCratesText: TextView
    private lateinit var lowStockList: TextView
    private lateinit var locationAnalyticsText: TextView
    private lateinit var categoryInsightsText: TextView
    private lateinit var fabBack: FloatingActionButton
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

    private lateinit var database: DatabaseReference
    private val itemsList = mutableListOf<StoreroomItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_analytics)

        // Initialize views
        totalEntriesText = findViewById(R.id.totalEntriesText)
        totalItemsText = findViewById(R.id.totalItemsText)
        uniqueProductsText = findViewById(R.id.uniqueProductsText)
        uniqueCratesText = findViewById(R.id.uniqueCratesText)
        lowStockList = findViewById(R.id.lowStockList)
        locationAnalyticsText = findViewById(R.id.locationAnalyticsText)
        categoryInsightsText = findViewById(R.id.categoryInsightsText)
        fabBack = findViewById(R.id.fabBack)
        toolbar = findViewById(R.id.toolbar)

        // Setup Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().getReference("storeroom_items")

        loadDataFromFirebase()

        fabBack.setOnClickListener {
            finish()
        }
    }

    private fun loadDataFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemsList.clear()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(StoreroomItem::class.java)
                    if (item != null) {
                        item.id = itemSnapshot.key ?: ""
                        itemsList.add(item)
                    }
                }
                calculateStatistics()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AnalyticsActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun calculateStatistics() {
        if (itemsList.isEmpty()) {
            updateEmptyState()
            return
        }

        // 📊 Total Entries
        totalEntriesText.text = itemsList.size.toString()

        // 📈 Total Items in Stock
        val totalQuantity = itemsList.sumOf { it.itemCount }
        totalItemsText.text = totalQuantity.toString()

        // 🔢 Unique Products
        val uniqueProducts = itemsList.map { it.itemName.lowercase().trim() }.distinct().size
        uniqueProductsText.text = uniqueProducts.toString()

        // 📦 Unique Crates
        val uniqueCrates = itemsList.map { it.crateNumber }.distinct().size
        uniqueCratesText.text = uniqueCrates.toString()

        // ⚠️ Low Stock Alerts (< 10 units)
        val lowStockItems = itemsList.filter { it.itemCount < 10 }
        if (lowStockItems.isNotEmpty()) {
            val sb = StringBuilder()
            lowStockItems.forEach {
                sb.append("• ${it.itemName}: ${it.itemCount} units (Crate ${it.crateNumber})\n")
            }
            lowStockList.text = sb.toString().trim()
            lowStockList.setTextColor(android.graphics.Color.RED)
        } else {
            lowStockList.text = "All items are well stocked!"
            lowStockList.setTextColor(android.graphics.Color.BLACK)
        }

        // 📍 Location Analytics
        val locationCounts = itemsList.groupBy { it.location }.mapValues { it.value.size }
        val locationSb = StringBuilder()
        locationCounts.entries.sortedByDescending { it.value }.forEach {
            locationSb.append("${it.key ?: "Unknown"}: ${it.value} entries\n")
        }
        locationAnalyticsText.text = locationSb.toString().trim()

        // 🏆 Top Category Insights
        val categoryCounts = itemsList.groupBy { it.crateName }.mapValues { it.value.sumOf { item -> item.itemCount } }
        val categorySb = StringBuilder()
        categoryCounts.entries.sortedByDescending { it.value }.take(10).forEach {
            categorySb.append("${it.key ?: "Uncategorized"}: ${it.value} items total\n")
        }
        categoryInsightsText.text = categorySb.toString().trim()
    }

    private fun updateEmptyState() {
        totalEntriesText.text = "0"
        totalItemsText.text = "0"
        uniqueProductsText.text = "0"
        uniqueCratesText.text = "0"
        lowStockList.text = "No items in inventory"
        locationAnalyticsText.text = "No data"
        categoryInsightsText.text = "No data"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
