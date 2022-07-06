package com.example.wishlist.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.wishlist.Geofence
import com.example.wishlist.MainActivity
import com.example.wishlist.database.ItemDb
import com.example.wishlist.database.ItemDto
import com.example.wishlist.databinding.FragmentAddBinding
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.concurrent.thread

private const val REQUEST_IMAGE = 1
private const val REQUEST_CAMERA = 2


class AddItemFragment : Fragment() {

    private lateinit var binding: FragmentAddBinding
    private var imageName: String? = null
    private var location: Location? = null
    private var latLng: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentAddBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpTakePhoto()
        if(arguments?.isEmpty == false) {
            val latLng: LatLng = arguments?.get("location") as LatLng
            this.latLng = latLng
            setLocationLabel(latLng)
        }

        setUpImage()
        setUpLocationButton()
        setUpButtonSave()
    }

    private fun setUpLocationButton(){
        binding.locationButton.setOnClickListener {
            (activity as MainActivity).navigateToMapFragment(this)
        }
    }

    private fun setUpImage(){
        if(imageName!=null){
            var bitmap =  BitmapFactory.decodeFile(imageName?.let {
                requireContext().filesDir.resolve(
                    it
                ).absolutePath
            })
            bitmap=Bitmap.createScaledBitmap(bitmap,800,800,true)
            binding.image.setImageBitmap(bitmap)
        }
    }

    private fun setUpButtonSave(){
        binding.addButton.setOnClickListener {
            if(binding.ItemName.text.isNotEmpty() && imageName!=null && location!=null) {
                val newItem = ItemDto(
                    itemName = binding.ItemName.text.toString(),
                    dateAdded = LocalDate.now(),
                    location = binding.location.text.toString(),
                    imagePath = requireContext().filesDir.resolve(imageName!!).absolutePath,
                    latitude = if (latLng != null) latLng!!.latitude else location!!.latitude,
                    longitude = if (latLng != null) latLng!!.longitude else location!!.longitude,
                )
                thread {
                    ItemDb.open(requireContext()).items.insert(newItem)
                    val lastInsertedKey = ItemDb.open(requireContext()).items.getLastInsertedKey()
                    val geofence: com.google.android.gms.location.Geofence =
                        if (latLng != null) {
                            Geofence(newItem).createGeofence(
                                latLng!!, lastInsertedKey, requireContext()
                            )
                        } else {
                            Geofence(newItem).createGeofence(
                                LatLng(
                                    location!!.latitude,
                                    location!!.longitude
                                ), lastInsertedKey, requireContext()
                            )
                        }
                    (activity as? MainActivity)?.addToGeofenceList(geofence)
                    (activity as? MainActivity)?.navigateToListItemsFragment()
                }
            }else{
                binding.exceptionInfo.text = "Fill all data!"
            }
        }

    }

    private fun setUpTakePhoto(){
        binding.image.setOnClickListener {
            checkPermission()
        }
    }

    private fun takePhoto(){
        val uri = generateUri()
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).let {
            it.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }
        startActivityForResult(intent, REQUEST_IMAGE)
    }

     private fun generateUri() : Uri {
        val file = requireContext().filesDir.resolve(getImageName()).also {
            it.writeText("")
        }
        return FileProvider.getUriForFile(
            requireContext(),
            "com.example.wishlist.FileProvider",
            file
        )
    }

    private fun checkPermission(){
        if(ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(),Array(1) { android.Manifest.permission.CAMERA },
                REQUEST_CAMERA)
        }else{
            takePhoto()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if( requestCode == REQUEST_CAMERA){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                takePhoto()
            }
        }
    }

    private fun getImageName(): String {
        val date = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        imageName = "$date.img"
        return "$date.img"
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            val location : Location? = (activity as MainActivity).getLocation()
            latLng = null
            if (location != null) {
                setLocationLabel(location)
            }
            var bitmap =  BitmapFactory.decodeFile(imageName?.let {
                requireContext().filesDir.resolve(
                    it
                ).absolutePath
            })
            bitmap=Bitmap.createScaledBitmap(bitmap,800,800,true)
            binding.image.setImageBitmap(bitmap)
        }
    }
    private fun setLocationLabel(location: Location ){
        this.location = location
        val addresses= Geocoder(requireContext()).getFromLocation(location.latitude, location.longitude,1)
        var locationName : String = addresses?.get(0)?.getAddressLine(0)?.split(",")?.get(0) ?:""
        locationName += ", " + addresses?.get(0)?.locality
        locationName += ", " + addresses?.get(0)?.countryName
        binding.location.text = locationName

    }

     private fun setLocationLabel(latLng: LatLng ){
        val addresses= Geocoder(requireContext()).getFromLocation(latLng.latitude, latLng.longitude,1)
        var locationName : String = addresses?.get(0)?.getAddressLine(0)?.split(",")?.get(0) ?:""
        locationName += ", " + addresses?.get(0)?.locality
        locationName += ", " + addresses?.get(0)?.countryName
        binding.location.text = locationName
         println(locationName)
    }


}