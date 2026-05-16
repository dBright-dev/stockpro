package com.dbrightsites.stockapp

data class ItemHistory(
    var id: String = "",
    var itemId: String = "",           // Original item ID
    var action: String = "",            // "ADDED", "UPDATED", "DELETED"
    var timestamp: Long = 0,            // When the change happened
    var dateFormatted: String = "",     // Human readable date

    // Changes tracked
    var previousCrateNumber: String = "",
    var newCrateNumber: String = "",
    var previousCrateName: String = "",
    var newCrateName: String = "",
    var previousItemName: String = "",
    var newItemName: String = "",
    var previousItemCount: Int = 0,
    var newItemCount: Int = 0,
    var previousLocation: String = "",
    var newLocation: String = "",
    var previousNotes: String = "",
    var newNotes: String = "",
    var previousDateChecked: String = "",
    var newDateChecked: String = "",

    // Summary of changes
    var changeSummary: String = ""
)