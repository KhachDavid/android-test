package com.example.fetchlist.network

import com.example.fetchlist.model.Item
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("hiring.json")
    suspend fun getItems(): Response<List<Item>>
}
