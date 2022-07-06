package com.example.wishlist.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream
import java.time.LocalDate

class DateConverter {
    @TypeConverter
    fun toDate(timestamp: Long): LocalDate {
        return LocalDate.ofEpochDay(timestamp)
    }

    @TypeConverter
    fun toTimestamp(date: LocalDate): Long{
        return date.toEpochDay()
    }

    @TypeConverter
    fun toByteArray(bitmap: Bitmap) : ByteArray{
        val stream = ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,0,stream)
        stream.close()
        return stream.toByteArray()
    }
    @TypeConverter
    fun toBitmap(byteArray: ByteArray) : Bitmap {
        val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
        return bitmap
    }
}