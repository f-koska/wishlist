package com.example.wishlist

import android.graphics.BitmapFactory
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.HandlerCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.wishlist.database.ItemDb
import com.example.wishlist.database.ItemDto
import com.example.wishlist.databinding.ItemBinding
import com.example.wishlist.fragments.ListFragment
import kotlin.concurrent.thread

class ItemViewHolder(private val view : ItemBinding) : RecyclerView.ViewHolder(view.root) {

    fun bind(item : ItemDto){
        with(view){
            itemName.text = item.itemName
            locationItem.text = item.location
            image.setImageBitmap(BitmapFactory.decodeFile(item.imagePath))
        }
    }
}

class ItemAdapter(private val db: ItemDb, private val listFragment: ListFragment) : RecyclerView.Adapter<ItemViewHolder>() {

    private var items = mutableListOf<ItemDto>()
    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = ItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
        val id = items[position].id
        holder.itemView.setOnClickListener {
            (listFragment.activity as MainActivity).navigateToEditItemFragment(id)

        }
    }

    override fun getItemCount(): Int = items.size

    fun load(itemsList: List<ItemDto>){
        items.clear()
        items.addAll(itemsList)
        mainHandler.post{
            notifyDataSetChanged()
        }
    }

    fun load(){
        thread {
            items = db.items.getAll() as MutableList<ItemDto>
        }
        mainHandler.post{
            notifyDataSetChanged()
        }
    }

    fun getItems(): MutableList<ItemDto>{
        return items
    }


}