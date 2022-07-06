package com.example.wishlist

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.wishlist.Notifaction.Notification.createItemNotification
import com.google.android.gms.location.*

class MyService : Service() {
    private lateinit var locationClient: FusedLocationProviderClient

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationServices.getFusedLocationProviderClient(this)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if(intent!=null) {
            startForeground(1, intent.let { createItemNotification(this, intent.extras?.get("itemName") as String,intent.extras?.get("imagePath") as String) })
        }

        return START_STICKY
    }

    private val callback = object : LocationCallback() {
        override fun onLocationResult(location: LocationResult) {
            locationClient.removeLocationUpdates(this)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        locationClient.removeLocationUpdates(callback)
    }
}