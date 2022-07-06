package com.example.wishlist

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.wishlist.database.ItemDto
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

private const val GEOFENCE_PENDING_INTENT_REQUEST_CODE = 10

class Geofence(val item : ItemDto) {

    @SuppressLint("MissingPermission")
    fun createGeofence( location: LatLng, id:Long, context: Context ): Geofence {

        val geofence = Geofence.Builder()
            .setCircularRegion(location.latitude, location.longitude, 400f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setRequestId(id.toString())
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
        val request = GeofencingRequest.Builder().addGeofence(geofence).setInitialTrigger(Geofence.GEOFENCE_TRANSITION_EXIT).build()

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)).
            let { context.startActivity(it) }
        } else {
            LocationServices.getGeofencingClient(context).addGeofences(request, makePendingIntent(context))
        }
        return geofence
    }
    private fun makePendingIntent(context: Context) : PendingIntent {
        val intent = Intent(context, LocationReceiver::class.java)
        intent.putExtra("imagePath", item.imagePath)
        intent.putExtra("itemName", item.itemName)
        intent.putExtra("id", item.id)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            GEOFENCE_PENDING_INTENT_REQUEST_CODE,
           intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        return pendingIntent
    }

}



