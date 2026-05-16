package com.dbrightsites.stockapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.text.TextWatcher
import android.widget.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {
    // Declare views

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddItem: FloatingActionButton
    private lateinit var totalCratesText: TextView
    private lateinit var totalItemsStockText: TextView
    private lateinit var filterStatsText: TextView


    private lateinit var searchInput: EditText
    private lateinit var categoryFilter: AutoCompleteTextView
    private lateinit var locationFilter: AutoCompleteTextView
    private lateinit var categoryFilterButton: Button
    private lateinit var locationFilterButton: Button
    private lateinit var clearFiltersButton: Button

    private lateinit var database: DatabaseReference
    private lateinit var adapter: StoreroomAdapter
    private val itemsList = mutableListOf<StoreroomItem>()
    private val filteredItemsList = mutableListOf<StoreroomItem>()  // Filtered list for display

    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    // ArrayLists for dropdowns
    private val crateNumbers = mutableListOf("CR01", "CR02", "CR03", "CR04", "CR05")
    // Complete Crate Name/Category list with all your items
    private val crateCategories = mutableListOf(
        "All",
        "Candles & Matches",
        "Awareness",
        "Decorations (Cares)",
        "Blankets",
        "Display (Open Day)",
        "Crocheting",
        "LGBTQIA+",
        "LGBTQIA+ Closest",
        "Baking",
        "Party Box",
        "Literacy",
        "Paints & Spray",
        "Games",
        "Paint Brushes",
        "Aprons",
        "Paint Plastics",
        "Clothes (Donation)",
        "Shoes (Donation)",
        "Bottle Tops",
        "CDs (Donation)",
        "Constitution",
        "Books (Donation)",
        "Toys (Donation)",
        "JS Decorations"
    )

    private val locations = mutableListOf("All","Row A", "Row B", "Row C", "Row D", "Row E", "Row F (Floor)")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        try {
            setContentView(R.layout.activity_main)

            // Initialize views
            recyclerView = findViewById(R.id.recyclerView)
            fabAddItem = findViewById(R.id.fabAddItem)
            // Initialize stats TextViews
            totalCratesText = findViewById(R.id.totalCratesText)
            totalItemsStockText = findViewById(R.id.totalItemsStockText)
            filterStatsText = findViewById(R.id.filterStatsText)
            searchInput = findViewById(R.id.searchInput)
            categoryFilter = findViewById(R.id.categoryFilter)
            locationFilter = findViewById(R.id.locationFilter)
            //categoryFilterButton = findViewById(R.id.categoryFilterButton)
            //locationFilterButton = findViewById(R.id.locationFilterButton)
            clearFiltersButton = findViewById(R.id.clearFiltersButton)


            // Sort categories alphabetically and add "All" at the beginning
            crateCategories.sort()
            val categoriesWithoutAll = crateCategories.filter { it != "All" }.sorted()
            crateCategories.clear()
            crateCategories.add("All")
            crateCategories.addAll(categoriesWithoutAll)

            val locationsWithoutAll = locations.filter { it != "All" }.sorted()
            locations.clear()
            locations.add("All")
            locations.addAll(locationsWithoutAll)

            // Load saved lists from Firebase
            loadDropdownListsFromFirebase()

            // Setup filters
            setupFilters()

            // Initialize Firebase with error handling
            try {
                database = FirebaseDatabase.getInstance().getReference("storeroom_items")
                Toast.makeText(this, "Firebase connected", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Firebase error: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }

            setupRecyclerView()
            loadItemsFromFirebase()

            // Setup search listener
            setupSearchListener()

            // FAB click listener
            fabAddItem.setOnClickListener {
                showAddItemDialog()
            }
            val fabHistory = findViewById<FloatingActionButton>(R.id.fabHistory)
            fabHistory.setOnClickListener {
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
            }

            val fabAnalytics = findViewById<FloatingActionButton>(R.id.fabAnalytics)
            fabAnalytics.setOnClickListener {
                val intent = Intent(this, AnalyticsActivity::class.java)
                startActivity(intent)
            }

            // Clear filters button
            clearFiltersButton.setOnClickListener {
                clearAllFilters()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "App error: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            finish()
        }
    }

    private fun setupFilters() {
        // Make filter fields not editable
        categoryFilter.isEnabled = true
        locationFilter.isEnabled = true

        // Category filter button
        categoryFilter.setOnClickListener {
            showFilterDialog("Select Category", crateCategories) { selected ->
                categoryFilter.setText(selected)
                applyFilters()
            }
        }

        // Location filter button
        locationFilter.setOnClickListener {
            showFilterDialog("Select Location", locations) { selected ->
                locationFilter.setText(selected)
                applyFilters()
            }
        }
    }

    private fun showFilterDialog(title: String, items: MutableList<String>, onSelect: (String) -> Unit) {
        val itemsArray = items.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(itemsArray) { _, which ->
                onSelect(itemsArray[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupSearchListener() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun applyFilters() {
        val searchQuery = searchInput.text.toString().trim().lowercase()
        val selectedCategory = categoryFilter.text.toString()
        val selectedLocation = locationFilter.text.toString()

        filteredItemsList.clear()

        for (item in itemsList) {
            var matches = true

            // Apply search filter
            if (searchQuery.isNotEmpty()) {
                val matchesSearch = item.itemName.lowercase().contains(searchQuery) ||
                        item.crateNumber.lowercase().contains(searchQuery) ||
                        item.crateName.lowercase().contains(searchQuery) ||
                        item.notes.lowercase().contains(searchQuery)
                if (!matchesSearch) matches = false
            }

            // Apply category filter
            if (matches && selectedCategory.isNotEmpty() && selectedCategory != "All") {
                if (item.crateName != selectedCategory) matches = false
            }

            // Apply location filter
            if (matches && selectedLocation.isNotEmpty() && selectedLocation != "All") {
                if (item.location != selectedLocation) matches = false
            }

            if (matches) {
                filteredItemsList.add(item)
            }
        }

        adapter.updateItems(filteredItemsList)
        updateStats()

        // Show filter result message
        val filterCount = filteredItemsList.size
        Toast.makeText(this, "Showing $filterCount of ${itemsList.size} items", Toast.LENGTH_SHORT).show()
    }

    private fun clearAllFilters() {
        searchInput.text.clear()
        categoryFilter.text.clear()
        locationFilter.text.clear()
        applyFilters()
        Toast.makeText(this, "All filters cleared", Toast.LENGTH_SHORT).show()
    }

    private fun updateStats() {
        val totalItems = filteredItemsList.size
        val totalQuantity = calculateTotalItemsInStock(filteredItemsList)
        val totalCrates = calculateTotalCrates(filteredItemsList)

        // Update stats displays
        totalCratesText.text = totalCrates.toString()
        totalItemsStockText.text = totalQuantity.toString()

        // Update filter stats text
        if (filteredItemsList.size != itemsList.size) {
            filterStatsText.text = "Showing ${filteredItemsList.size} of ${itemsList.size} items (filtered)"
        } else {
            filterStatsText.text = "Showing all ${itemsList.size} items"
        }
    }

    // Add this method to calculate total crates (unique crate numbers)
    private fun calculateTotalCrates(items: List<StoreroomItem>): Int {
        val uniqueCrates = mutableSetOf<String>()
        for (item in items) {
            uniqueCrates.add(item.crateNumber)
        }
        return uniqueCrates.size
    }

    // Add this method to calculate total items in stock (sum of all quantities)
    private fun calculateTotalItemsInStock(items: List<StoreroomItem>): Int {
        var total = 0
        for (item in items) {
            total += item.itemCount
        }
        return total
    }

    private fun loadDropdownListsFromFirebase() {
        val listsRef = FirebaseDatabase.getInstance().getReference("dropdown_lists")

        // Load crate numbers
        listsRef.child("crate_numbers").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val savedNumbers = mutableListOf<String>()
                for (child in snapshot.children) {
                    child.getValue(String::class.java)?.let { savedNumbers.add(it) }
                }
                if (savedNumbers.isNotEmpty()) {
                    crateNumbers.clear()
                    crateNumbers.addAll(savedNumbers)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Load crate categories
        listsRef.child("crate_categories").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val savedCategories = mutableListOf<String>()
                for (child in snapshot.children) {
                    child.getValue(String::class.java)?.let { savedCategories.add(it) }
                }
                if (savedCategories.isNotEmpty()) {
                    crateCategories.clear()
                    crateCategories.addAll(savedCategories)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // Load locations
        listsRef.child("locations").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val savedLocations = mutableListOf<String>()
                for (child in snapshot.children) {
                    child.getValue(String::class.java)?.let { savedLocations.add(it) }
                }
                if (savedLocations.isNotEmpty()) {
                    locations.clear()
                    locations.addAll(savedLocations)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun saveDropdownListsToFirebase() {
        val listsRef = FirebaseDatabase.getInstance().getReference("dropdown_lists")

        // Save crate numbers
        listsRef.child("crate_numbers").setValue(crateNumbers)

        // Save crate categories
        listsRef.child("crate_categories").setValue(crateCategories)

        // Save locations
        listsRef.child("locations").setValue(locations)
    }

    private fun setupRecyclerView() {
        adapter = StoreroomAdapter(
            items = itemsList,
            onEditClick = { item -> showEditDialog(item) },
            onDeleteClick = { item -> deleteItem(item) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadItemsFromFirebase() {
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
                // Sort by timestamp descending (newest first)
                itemsList.sortByDescending { it.timestamp }
                adapter.updateItems(itemsList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddItemDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)

        val dateCheckedInput = dialogView.findViewById<EditText>(R.id.dateCheckedInput)
        val crateNumberSpinner = dialogView.findViewById<AutoCompleteTextView>(R.id.crateNumberSpinner)
        val crateNameSpinner = dialogView.findViewById<AutoCompleteTextView>(R.id.crateNameSpinner)
        val itemNameInput = dialogView.findViewById<EditText>(R.id.itemNameInput)
        val itemCountInput = dialogView.findViewById<EditText>(R.id.itemCountInput)
        val locationSpinner = dialogView.findViewById<AutoCompleteTextView>(R.id.locationSpinner)
        val notesInput = dialogView.findViewById<EditText>(R.id.notesInput)

        val crateNumberButton = dialogView.findViewById<Button>(R.id.crateNumberButton)
        val crateNameButton = dialogView.findViewById<Button>(R.id.crateNameButton)
        val locationButton = dialogView.findViewById<Button>(R.id.locationButton)

        // Set current date
        dateCheckedInput.setText(displayDateFormat.format(Date()))
        dateCheckedInput.isEnabled = false

        // Setup spinners
        setupSpinner(crateNumberSpinner, crateNumbers)
        setupSpinner(crateNameSpinner, crateCategories)
        setupSpinner(locationSpinner, locations)

        // Setup button listeners
        crateNumberButton.setOnClickListener {
            showDropdownDialog("Select Crate Number", crateNumbers) { selected ->
                crateNumberSpinner.setText(selected, false)
            }
        }

        crateNameButton.setOnClickListener {
            showDropdownDialog("Select Crate Category", crateCategories) { selected ->
                crateNameSpinner.setText(selected, false)
            }
        }

        locationButton.setOnClickListener {
            showDropdownDialog("Select Location", locations) { selected ->
                locationSpinner.setText(selected, false)
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Add Item") { _, _ ->
                val crateNumber = crateNumberSpinner.text.toString().trim()
                val crateName = crateNameSpinner.text.toString().trim()
                val itemName = itemNameInput.text.toString().trim()
                val itemCount = itemCountInput.text.toString().trim()
                val location = locationSpinner.text.toString().trim()
                val notes = notesInput.text.toString().trim()

                if (validateInputs(crateNumber, crateName, itemName, itemCount)) {
                    addItemToFirebase(
                        crateNumber = crateNumber,
                        crateName = crateName,
                        itemName = itemName,
                        itemCount = itemCount.toInt(),
                        location = location,
                        notes = notes
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        // Style the positive button
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.purple_700))
    }

    private fun showDropdownDialog(title: String, items: MutableList<String>, onSelect: (String) -> Unit) {
        val itemsArray = items.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(itemsArray) { _, which ->
                onSelect(itemsArray[which])
            }
            .setPositiveButton("Add New") { _, _ ->
                showAddNewItemDialog(title, items) { newItem ->
                    onSelect(newItem)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddNewItemDialog(title: String, items: MutableList<String>, onAdd: (String) -> Unit) {
        val input = EditText(this)
        input.hint = "Enter new ${title.lowercase()}"

        AlertDialog.Builder(this)
            .setTitle("Add New $title")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val newItem = input.text.toString().trim()
                if (newItem.isNotEmpty() && !items.contains(newItem)) {
                    items.add(newItem)
                    items.sort()
                    saveDropdownListsToFirebase()
                    onAdd(newItem)
                    Toast.makeText(this, "Added: $newItem", Toast.LENGTH_SHORT).show()
                } else if (items.contains(newItem)) {
                    Toast.makeText(this, "Item already exists!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter a valid item", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupSpinner(spinner: AutoCompleteTextView, items: MutableList<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
        spinner.setAdapter(adapter)
        spinner.threshold = 1
        spinner.setOnClickListener {
            spinner.showDropDown()
        }
    }

    private fun validateInputs(crateNumber: String, crateName: String, itemName: String, itemCount: String): Boolean {
        return when {
            crateNumber.isEmpty() -> {
                Toast.makeText(this, "Please select a crate number", Toast.LENGTH_SHORT).show()
                false
            }
            crateName.isEmpty() -> {
                Toast.makeText(this, "Please select a crate category", Toast.LENGTH_SHORT).show()
                false
            }
            itemName.isEmpty() -> {
                Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show()
                false
            }
            itemCount.isEmpty() -> {
                Toast.makeText(this, "Please enter an item count", Toast.LENGTH_SHORT).show()
                false
            }
            itemCount.toIntOrNull() == null -> {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun addItemToFirebase(crateNumber: String, crateName: String, itemName: String,
                                  itemCount: Int, location: String, notes: String) {
        val currentDate = displayDateFormat.format(Date())
        val timestamp = System.currentTimeMillis()

        val item = StoreroomItem(
            date = currentDate,
            crateNumber = crateNumber,
            crateName = crateName,
            itemName = itemName,
            itemCount = itemCount,
            location = location,
            notes = notes,
            timestamp = timestamp
        )

        val newItemRef = database.push()
        item.id = newItemRef.key ?: ""

        newItemRef.setValue(item)
            .addOnSuccessListener {
                // Save to history
                saveToHistory(item, "ADDED")
                Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add item: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditDialog(item: StoreroomItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_item, null)

        val crateNumberInput = dialogView.findViewById<AutoCompleteTextView>(R.id.editCrateNumber)
        val crateNameInput = dialogView.findViewById<AutoCompleteTextView>(R.id.editCrateName)
        val itemNameInput = dialogView.findViewById<EditText>(R.id.editItemName)
        val itemCountInput = dialogView.findViewById<EditText>(R.id.editItemCount)
        val locationInput = dialogView.findViewById<AutoCompleteTextView>(R.id.editLocation)
        val notesInput = dialogView.findViewById<EditText>(R.id.editNotes)

        setupSpinner(crateNumberInput, crateNumbers)
        setupSpinner(crateNameInput, crateCategories)
        setupSpinner(locationInput, locations)

        crateNumberInput.setText(item.crateNumber, false)
        crateNameInput.setText(item.crateName, false)
        locationInput.setText(item.location, false)
        itemNameInput.setText(item.itemName)
        itemCountInput.setText(item.itemCount.toString())
        notesInput.setText(item.notes)

        AlertDialog.Builder(this)
            .setTitle("Edit Item")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updatedCrateNumber = crateNumberInput.text.toString().trim()
                val updatedCrateName = crateNameInput.text.toString().trim()
                val updatedItemName = itemNameInput.text.toString().trim()
                val updatedItemCount = itemCountInput.text.toString().trim().toIntOrNull()
                val updatedLocation = locationInput.text.toString().trim()
                val updatedNotes = notesInput.text.toString().trim()

                if (updatedCrateNumber.isNotEmpty() && updatedCrateName.isNotEmpty() &&
                    updatedItemName.isNotEmpty() && updatedItemCount != null) {
                    updateItem(
                        item = item,
                        crateNumber = updatedCrateNumber,
                        crateName = updatedCrateName,
                        itemName = updatedItemName,
                        itemCount = updatedItemCount,
                        location = updatedLocation,
                        notes = updatedNotes
                    )
                } else {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateItem(item: StoreroomItem, crateNumber: String, crateName: String,
                           itemName: String, itemCount: Int, location: String, notes: String) {

        // Get the old item data first
        val oldItem = item.copy()

        val updates = mapOf(
            "dateLastChecked" to displayDateFormat.format(Date()),
            "crateNumber" to crateNumber,
            "crateName" to crateName,
            "itemName" to itemName,
            "itemCount" to itemCount,
            "location" to location,
            "notes" to notes,
            "timestamp" to System.currentTimeMillis()
        )

        database.child(item.id).updateChildren(updates)
            .addOnSuccessListener {
                // Save to history with old data
                val updatedItem = item.apply {
                    this.crateNumber = crateNumber
                    this.crateName = crateName
                    this.itemName = itemName
                    this.itemCount = itemCount
                    this.location = location
                    this.notes = notes
                    this.date = displayDateFormat.format(Date())
                    this.timestamp = System.currentTimeMillis()
                }
                saveToHistory(updatedItem, "UPDATED", oldItem)
                Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteItem(item: StoreroomItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Delete") { _, _ ->
                // Save to history before deleting
                saveToHistory(item, "DELETED")
                database.child(item.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveToHistory(item: StoreroomItem, action: String, oldItem: StoreroomItem? = null) {
        val historyRef = FirebaseDatabase.getInstance().getReference("item_history")
        val timestamp = System.currentTimeMillis()
        val dateFormatted = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(Date())

        val history = ItemHistory(
            itemId = item.id,
            action = action,
            timestamp = timestamp,
            dateFormatted = dateFormatted
        )

        when (action) {
            "ADDED" -> {
                history.newCrateNumber = item.crateNumber
                history.newCrateName = item.crateName
                history.newItemName = item.itemName
                history.newItemCount = item.itemCount
                history.newLocation = item.location
                history.newNotes = item.notes
                history.newDateChecked = item.date
                history.changeSummary = "New item added: ${item.itemName} (${item.itemCount})"
            }

            "UPDATED" -> {
                oldItem?.let { old ->
                    history.previousCrateNumber = old.crateNumber
                    history.newCrateNumber = item.crateNumber
                    history.previousCrateName = old.crateName
                    history.newCrateName = item.crateName
                    history.previousItemName = old.itemName
                    history.newItemName = item.itemName
                    history.previousItemCount = old.itemCount
                    history.newItemCount = item.itemCount
                    history.previousLocation = old.location
                    history.newLocation = item.location
                    history.previousNotes = old.notes
                    history.newNotes = item.notes
                    history.previousDateChecked = old.date
                    history.newDateChecked = item.date

                    val changes = mutableListOf<String>()
                    if (old.itemCount != item.itemCount) changes.add("quantity changed from ${old.itemCount} to ${item.itemCount}")
                    if (old.crateName != item.crateName) changes.add("category changed")
                    if (old.location != item.location) changes.add("location changed")
                    val changeText = if (changes.isNotEmpty()) changes.joinToString(", ") else "details updated"
                    history.changeSummary = "Updated: ${item.itemName} - $changeText"
                }
            }

            "DELETED" -> {
                history.previousCrateNumber = item.crateNumber
                history.previousCrateName = item.crateName
                history.previousItemName = item.itemName
                history.previousItemCount = item.itemCount
                history.previousLocation = item.location
                history.previousNotes = item.notes
                history.previousDateChecked = item.date
                history.changeSummary = "Deleted: ${item.itemName} (was ${item.itemCount} )"
            }
        }

        historyRef.push().setValue(history)
            .addOnFailureListener { e ->
                println("Failed to save history: ${e.message}")
            }
    }
}