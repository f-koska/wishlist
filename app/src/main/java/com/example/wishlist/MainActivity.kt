package com.example.wishlist

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.wishlist.Notifaction.Notification
import com.example.wishlist.fragments.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

private const val GEOFENCE_PENDING_INTENT_REQUEST_CODE = 10

class MainActivity : AppCompatActivity(), LocationListener{
    private lateinit var listFragment: ListFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var location: Location? = null
    private var geofenceList: MutableList<Geofence> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        supportActionBar?.hide()
        listFragment = ListFragment()
        supportFragmentManager.beginTransaction().add(R.id.container, listFragment, listFragment.javaClass.name).commit()
        setupLocation()
        Notification.createChannel(this)
       // deleteAllGeofence(this)
        //loadGeofence()
    }

     fun navigateToAddItemFragment(){
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, AddItemFragment(), AddItemFragment::class.java.name)
            .addToBackStack(ListFragment::class.java.name)
            .commit()
    }

    fun navigateToListItemsFragment(){
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, ListFragment(), ListFragment::class.java.name)
            .commit()
    }

    fun navigateToEditItemFragment(id : Long){
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, EditItemFragment(id ), EditItemFragment::class.java.name)
            .addToBackStack(ListFragment::class.java.name)
            .commit()
    }

    fun navigateToMapFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, MapFragment(fragment), MapFragment::class.java.name)
            .addToBackStack(fragment::class.java.name)
            .commit()
    }

    @SuppressLint("MissingPermission")
    fun setupLocation(){
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permissions ->
        }.launch(arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ))

        val locationManager = getSystemService(LocationManager::class.java)
            .requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                100,
                500f,
                this)
    }

    fun getLocation(): Location?{
        return location
    }

    fun addToGeofenceList(geofence: Geofence){
        geofenceList.add(geofence)
    }

    fun removeGeofenceFromGeofenceList(id: Long){
        var geofenceToRemove : Geofence;
        for(geofence in geofenceList){
            if(geofence.requestId == id.toString()){
                geofenceList.remove(geofence)
                LocationServices.getGeofencingClient(this).removeGeofences(mutableListOf(id.toString()))
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        this.location = location
    }

    fun deleteAllGeofence(context: Context){
        val intent = Intent(context, LocationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, GEOFENCE_PENDING_INTENT_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
        LocationServices.getGeofencingClient(context).removeGeofences(pendingIntent)
    }

    fun loadGeofence(){
        for(item in listFragment.getItemAdapter()?.getItems()!!){
            val geofence = Geofence(item).createGeofence(LatLng(item.latitude,item.longitude), item.id, this)
                addToGeofenceList(geofence)
        }
    }

}