package com.example.fetchlist

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fetchlist.adapter.ItemAdapter
import com.example.fetchlist.databinding.ActivityMainBinding
import com.example.fetchlist.repository.ItemRepository
import com.example.fetchlist.viewmodel.ItemViewModel
import com.example.fetchlist.viewmodel.ItemViewModelFactory
import com.example.fetchlist.viewmodel.Result

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ItemViewModel by viewModels {
        ItemViewModelFactory(ItemRepository())
    }
    private lateinit var adapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        adapter = ItemAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Observe LiveData
        viewModel.items.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                    binding.errorText.visibility = View.GONE
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.errorText.visibility = View.GONE
                    adapter.submitList(result.data.toList())
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.GONE
                    binding.errorText.visibility = View.VISIBLE
                    binding.errorText.text = result.message
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // Swipe to Refresh (Optional)
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchItems()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
}
