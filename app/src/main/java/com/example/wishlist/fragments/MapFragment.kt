package com.example.wishlist.fragments

import android.content.pm.PackageManager
import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.example.wishlist.MainActivity
import com.example.wishlist.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar

private const val REQUEST_LOCATION_PERMISSIONS = 1000

class MapFragment(fragment: Fragment) : Fragment(){

    private lateinit var map: GoogleMap
    private lateinit var latLng: LatLng
    private lateinit var fragment: Fragment

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback{ googleMap ->
        requestPermission()
        val location = (activity as MainActivity).getLocation()
        if(location != null) {
            val latlng = LatLng(location.latitude, location.longitude)
            googleMap.addMarker(MarkerOptions().position(latlng))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latlng))
        }

        requestPermission()
        map=googleMap.apply {
            setOnMapClickListener(::onMapClicked)
            googleMap.isMyLocationEnabled = true
        }

        this.fragment = fragment
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

    }

    private fun requestPermission() {
        if (checkSelfPermission(requireContext(),ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSIONS)
        }
    }

    private fun onMapClicked(latLng: LatLng) {
        if (checkSelfPermission(requireContext(),ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
            Snackbar.make(
                requireView().findViewById(R.id.root),
                "Save as item location? : " + getLocation(latLng) ,
                Snackbar.LENGTH_INDEFINITE
            ).apply {
                setAction("Save") {
                    setLatLng(latLng)
                    dismiss()
                }
            }.show()
            with(map) {
                clear()
                addMarker(MarkerOptions().position(latLng))
                animateCamera(CameraUpdateFactory.newLatLng(latLng))
            }
        }else{
            requestBackgroundPermission()
        }
    }

    private fun requestBackgroundPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            checkSelfPermission(requireContext(),ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Snackbar.make(
                requireView().findViewById(R.id.root),
                "Turn on Background Location",
                Snackbar.LENGTH_INDEFINITE
            ).apply {
                setAction("Settings") {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", requireContext().packageName, null)
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    dismiss()
                }
            }.show()
        }
    }
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                map.isMyLocationEnabled = true
            } else {
                Toast.makeText(
                    requireContext(),
                    "This APP requires LOCATION PERMISSION!",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun setLatLng(latLng: LatLng){
        this.latLng = latLng
        val location = Bundle()
        location.putParcelable("location", latLng)
        fragment.arguments = location
        fragmentManager?.popBackStack()
    }
    private fun getLocation(latLng: LatLng) : String{
        val addresses= Geocoder(requireContext()).getFromLocation(latLng.latitude, latLng.longitude,1)
        var locationName : String = addresses?.get(0)?.getAddressLine(0)?.split(",")?.get(0) ?:""
        locationName += ", " + addresses?.get(0)?.locality
        locationName += ", " + addresses?.get(0)?.countryName
        return locationName
    }



}