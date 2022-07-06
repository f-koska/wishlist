package com.example.wishlist.database

import androidx.room.*

@Dao
interface ItemDao {
    @Query("SELECT * FROM Items ORDER BY Items.dateAdded")
    fun getAll(): List<ItemDto>

    @Query("SELECT * FROM Items where id = :id")
    fun getItem(id: Long): ItemDto

    @Query("DELETE  FROM Items where id = :id")
    fun deleteItem(id: Long)

    @Query("SELECT id FROM ITEMS ORDER BY id DESC LIMIT 1")
    fun getLastInsertedKey(): Long

    @Delete()
    fun delete(item: ItemDto)

    @Insert()
    fun insert(item: ItemDto)

    @Update()
    fun update(item: ItemDto)

}