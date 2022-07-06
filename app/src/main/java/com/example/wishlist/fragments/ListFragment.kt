package com.example.wishlist.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wishlist.ItemAdapter
import com.example.wishlist.MainActivity
import com.example.wishlist.database.ItemDb
import com.example.wishlist.databinding.FragmentListBinding
import kotlin.concurrent.thread

class ListFragment: Fragment() {
    private lateinit var binding: FragmentListBinding
    private var itemAdapter : ItemAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentListBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        itemAdapter = ItemAdapter(ItemDb.open(requireContext()), this)

        requireActivity().runOnUiThread {
            loadList()
        }
            binding.wishlist.apply {
                adapter = itemAdapter
                layoutManager = LinearLayoutManager(view.context)
            }
        binding.buttonAdd.setOnClickListener {
            (activity as? MainActivity)?.navigateToAddItemFragment()
        }
    }

    private fun loadList(){
        thread {
            val items = ItemDb.open(requireContext()).items.getAll()
            itemAdapter?.load(items)
        }
    }

     fun getItemAdapter(): ItemAdapter?{
        return itemAdapter;
    }

    override fun onStart() {
        super.onStart()
        requireActivity().runOnUiThread { itemAdapter?.load() }
    }

}