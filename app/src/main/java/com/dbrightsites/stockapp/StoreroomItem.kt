package com.dbrightsites.stockapp

import com.google.firebase.database.PropertyName

data class StoreroomItem(
    @PropertyName("id")
    var id: String = "",

    @PropertyName("date")
    var date: String = "", // Format: "yyyy-MM-dd HH:mm:ss"

    @PropertyName("crateNumber")
    var crateNumber: String = "",

    @PropertyName("itemName")
    var itemName: String = "",

    @PropertyName("itemCount")
    var itemCount: Int = 0,

    @PropertyName("crateName")
    var crateName: String = "",        // Crate Name/Category

    @PropertyName("location")
    var location: String = "",          // Location

    @PropertyName("notes")
    var notes: String = "",             // Notes

    @PropertyName("timestamp")
    var timestamp: Long = System.currentTimeMillis()
)