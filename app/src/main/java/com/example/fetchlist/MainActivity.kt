package com.example.fetchlist

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Button
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

        // Initialize Error Layout Views
        val retryButton: Button = binding.retryButton

        // Handle Retry Button Click
        retryButton.setOnClickListener {
            viewModel.fetchItems()
        }

        // Observe LiveData
        viewModel.items.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                    binding.errorLayout.visibility = View.GONE
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.errorLayout.visibility = View.GONE
                    adapter.submitList(result.data.toList())
                }
                is Result.Error -> {
                    handleFailure(result.message)
                }
            }
        }

        // Swipe to Refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchItems()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun handleFailure(errorMessage: String) {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.errorLayout.visibility = View.VISIBLE

        binding.errorText.text = if (!isInternetAvailable(this)) {
            "No internet connection. Please check your network and try again."
        } else {
            "Server error: $errorMessage"
        }
    }

    // Check for Internet Connection
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}
