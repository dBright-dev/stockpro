package com.dbrightsites.stockapp

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
    private lateinit var exportExcelButton: ImageButton
    private lateinit var exportPdfButton: ImageButton
    private lateinit var filterAll: Button
    private lateinit var filterAdded: Button
    private lateinit var filterUpdated: Button
    private lateinit var filterDeleted: Button
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

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
        exportExcelButton = findViewById(R.id.exportExcelButton)
        exportPdfButton = findViewById(R.id.exportPdfButton)
        filterAll = findViewById(R.id.filterAll)
        filterAdded = findViewById(R.id.filterAdded)
        filterUpdated = findViewById(R.id.filterUpdated)
        filterDeleted = findViewById(R.id.filterDeleted)
        toolbar = findViewById(R.id.toolbar)

        // Setup Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

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

        exportExcelButton.setOnClickListener {
            exportToExcel()
        }

        exportPdfButton.setOnClickListener {
            exportToPdf()
        }
    }

    private val createExcelFile = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { saveExcelFile(it) }
    }

    private val createPdfFile = registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let { savePdfFile(it) }
    }

    private fun exportToExcel() {
        if (filteredHistoryList.isEmpty()) {
            Toast.makeText(this, "No history to export", Toast.LENGTH_SHORT).show()
            return
        }
        createExcelFile.launch("stock_history_${System.currentTimeMillis()}.csv")
    }

    private fun exportToPdf() {
        if (filteredHistoryList.isEmpty()) {
            Toast.makeText(this, "No history to export", Toast.LENGTH_SHORT).show()
            return
        }
        createPdfFile.launch("stock_history_${System.currentTimeMillis()}.pdf")
    }

    private fun saveExcelFile(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val writer = outputStream.bufferedWriter()
                // Header
                writer.write("Date,Action,Item,Crate,Location,Summary\n")
                // Data
                for (history in filteredHistoryList) {
                    val row = listOf(
                        history.dateFormatted,
                        history.action,
                        if (history.action == "ADDED" || history.action == "UPDATED") history.newItemName else history.previousItemName,
                        if (history.action == "ADDED" || history.action == "UPDATED") history.newCrateNumber else history.previousCrateNumber,
                        if (history.action == "ADDED" || history.action == "UPDATED") history.newLocation else history.previousLocation,
                        history.changeSummary.replace(",", ";") // Avoid breaking CSV
                    ).joinToString(",")
                    writer.write("$row\n")
                }
                writer.flush()
            }
            Toast.makeText(this, "Excel (CSV) exported successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to export Excel: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePdfFile(uri: Uri) {
        try {
            val pdfDocument = PdfDocument()
            val paint = Paint()
            val titlePaint = Paint()

            // Page settings
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            titlePaint.textSize = 18f
            titlePaint.isFakeBoldText = true
            canvas.drawText("Stock History Report", 20f, 40f, titlePaint)

            paint.textSize = 12f
            var yPosition = 70f

            for (history in filteredHistoryList) {
                if (yPosition > 800) {
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 40f
                }

                canvas.drawText("${history.dateFormatted} - ${history.action}", 20f, yPosition, paint)
                yPosition += 15f
                
                val itemName = if (history.action == "ADDED" || history.action == "UPDATED") history.newItemName else history.previousItemName
                canvas.drawText("Item: $itemName", 30f, yPosition, paint)
                yPosition += 15f

                canvas.drawText("Summary: ${history.changeSummary}", 30f, yPosition, paint)
                yPosition += 25f // Extra space between entries
            }

            pdfDocument.finishPage(page)

            contentResolver.openOutputStream(uri)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()
            Toast.makeText(this, "PDF exported successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to export PDF: ${e.message}", Toast.LENGTH_SHORT).show()
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}