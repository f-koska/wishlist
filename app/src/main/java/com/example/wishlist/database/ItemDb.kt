package com.example.wishlist.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ItemDto::class
    ],
    version = 1
)
@TypeConverters(DateConverter::class)
abstract class ItemDb : RoomDatabase(){
    abstract val items : ItemDao
    companion object{
        fun open(context: Context) =
            Room.databaseBuilder(
                context, ItemDb::class.java, "items"
            ).build()
    }
}