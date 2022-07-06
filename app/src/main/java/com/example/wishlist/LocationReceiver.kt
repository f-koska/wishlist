package com.example.wishlist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.wishlist.database.ItemDb
import com.example.wishlist.database.ItemDto
import com.google.android.gms.location.GeofencingEvent
import kotlin.concurrent.thread


class LocationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val int = Intent(context, MyService::class.java)
        if (intent != null) {
            int.putExtra("itemName", intent.extras?.get("itemName") as String)
            int.putExtra("imagePath", intent.extras?.get("imagePath") as String)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context?.startForegroundService(int)
            } else {
                context?.startService(int)
            }

        }
    }
}