package com.example.wishlist.Notifaction

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.example.wishlist.R

private const val CHANNEL_ID = "ID_CHANNEL"

object Notification {

    fun createChannel(context: Context){
        val channel = NotificationChannel(CHANNEL_ID, "Items ", NotificationManager.IMPORTANCE_HIGH)
        with(context.getSystemService(NotificationManager::class.java)){
            createNotificationChannel(channel)
        }
    }

    fun createItemNotification(context: Context, itemName : String, itemPath: String) : Notification {

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Wishlist")
            .setContentText("You are close to the item ($itemName) from your wishlist!")
            .setLargeIcon(BitmapFactory.decodeFile(itemPath))
            .build()
    }
}