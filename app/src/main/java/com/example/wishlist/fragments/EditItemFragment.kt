package com.example.wishlist.fragments

import android.app.Activity
import android.content.Intent
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
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.wishlist.Geofence
import com.example.wishlist.MainActivity
import com.example.wishlist.database.ItemDb
import com.example.wishlist.database.ItemDto
import com.example.wishlist.databinding.FragmentEditBinding
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

private const val REQUEST_IMAGE = 1

class EditItemFragment(var id : Long) : Fragment() {
    private lateinit var binding: FragmentEditBinding
    private lateinit var item: ItemDto
    private var imageName: String? = null
    private var latLng: LatLng? = null
    private var location: Location? = null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentEditBinding.inflate(inflater,container,false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        if(imageName!=null){
            var bitmap =  BitmapFactory.decodeFile(imageName?.let {
                requireContext().filesDir.resolve(
                    it
                ).absolutePath
            })
            bitmap=Bitmap.createScaledBitmap(bitmap,800,800,true)
            binding.image.setImageBitmap(bitmap)
        }

        setupDeleteButton()
        setUpSaveButton()
        setupTakePhoto()
        setupLocationButton()
    }

    private fun setupView(){
        thread {

            item = ItemDb.open(requireContext()).items.getItem(id)
            println(item.id)
            requireActivity().runOnUiThread {
            binding.itemName.setText(item.itemName)
            binding.location.setText(item.location, TextView.BufferType.EDITABLE)

                binding.image.setImageBitmap(
                     Bitmap.createScaledBitmap(
                    BitmapFactory.decodeFile(item.imagePath),
                      800,
                    800,
                    true
                     )
                )
                if(arguments?.isEmpty == false) {
                    val latLng: LatLng = arguments?.get("location") as LatLng
                    this.latLng = latLng
                    setLocationLabel(latLng)
                }
            }
        }

    }


    private fun setupLocationButton(){
        binding.locationButton.setOnClickListener {
            (activity as MainActivity).navigateToMapFragment(this)
        }
    }

    private fun setupDeleteButton(){
        binding.deleteButton.setOnClickListener {
            thread {
                ItemDb.open(requireContext()).items.deleteItem(id)
            }
            (activity as? MainActivity)?.removeGeofenceFromGeofenceList(id)
            (activity as? MainActivity)?.navigateToListItemsFragment()
        }
    }

    private fun setUpSaveButton(){
        binding.saveButton.setOnClickListener {
            item.itemName = binding.itemName.text.toString()
            item.location = binding.location.text.toString()
            item.imagePath = if(imageName!=null) this.requireContext().filesDir.resolve(imageName!!).absolutePath else item.imagePath
            if (latLng!=null || location !=null) {
                item.latitude = if (latLng != null) latLng!!.latitude else location!!.latitude
                item.longitude = if (latLng != null) latLng!!.longitude else location!!.longitude
                (activity as? MainActivity)?.removeGeofenceFromGeofenceList(item.id)
                val geofence: com.google.android.gms.location.Geofence =
                    if(latLng!=null){
                        Geofence(item).createGeofence(
                            latLng!!, item.id, requireContext()
                        )
                    }else {
                        Geofence(item).createGeofence(
                            LatLng(
                                location!!.latitude,
                                location!!.longitude
                            ), item.id, requireContext()
                        )
                    }
                (activity as? MainActivity)?.addToGeofenceList(geofence)
            }

            thread {
                ItemDb.open(requireContext()).items.update(item)
                (activity as? MainActivity)?.navigateToListItemsFragment()
            }
        }
    }

    private fun setupTakePhoto(){
        binding.image.setOnClickListener {
            val uri = generateUri()
            val intent =
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, uri)
            startActivityForResult(intent, REQUEST_IMAGE)
        }
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

    private fun getImageName(): String {
        val date = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        imageName = "$date.img"
        return "$date.img"
    }

    private fun setLocationLabel(latLng: LatLng ){
        val addresses= Geocoder(requireContext()).getFromLocation(latLng.latitude, latLng.longitude,1)
        var locationName : String = addresses?.get(0)?.getAddressLine(0)?.split(",")?.get(0) ?:""
        locationName += ", " + addresses?.get(0)?.locality
        locationName += ", " + addresses?.get(0)?.countryName
        thread {
            requireActivity().runOnUiThread {
                binding.location.text = locationName
            }
        }
        println(locationName)
    }

    private fun setLocationLabel(location: Location ){
        this.location = location
        val addresses= Geocoder(requireContext()).getFromLocation(location.latitude, location.longitude,1)
        var locationName : String = addresses?.get(0)?.getAddressLine(0)?.split(",")?.get(0) ?:""
        locationName += ", " + addresses?.get(0)?.locality
        locationName += ", " + addresses?.get(0)?.countryName
        binding.location.setText(locationName, TextView.BufferType.EDITABLE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
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
            bitmap= Bitmap.createScaledBitmap(bitmap,800,800,true)
            binding.image.setImageBitmap(bitmap)

        }
    }


}