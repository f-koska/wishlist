package com.example.wishlist.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDate

@Entity(tableName = "Items")
data class ItemDto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var itemName: String,
    @TypeConverters(DateConverter::class)
    var dateAdded: LocalDate,
    var location: String,
    var imagePath: String,
    var latitude: Double,
    var longitude: Double
)