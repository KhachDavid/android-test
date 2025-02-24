package com.example.fetchlist.model

data class Item(
    val id: Int,
    val listId: Int,
    val name: String? // Optional, as defined in the backedn
)
