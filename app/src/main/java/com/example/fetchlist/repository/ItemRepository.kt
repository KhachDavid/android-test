package com.example.fetchlist.repository

import com.example.fetchlist.model.Item
import com.example.fetchlist.network.RetrofitClient
import retrofit2.Response

class ItemRepository {
    suspend fun fetchItems(): Response<List<Item>> {
        return RetrofitClient.apiService.getItems()
    }
}