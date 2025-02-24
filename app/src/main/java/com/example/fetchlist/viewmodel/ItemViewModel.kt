package com.example.fetchlist.viewmodel

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.fetchlist.model.Item
import com.example.fetchlist.repository.ItemRepository
import kotlinx.coroutines.launch

sealed class Result {
    object Loading : Result()
    data class Success(val data: Map<Int, List<Item>>) : Result()
    data class Error(val message: String) : Result()
}

class ItemViewModel(private val repository: ItemRepository) : ViewModel() {

    private val _items = MutableLiveData<Result>()
    val items: LiveData<Result> = _items

    init {
        fetchItems()
    }

    fun fetchItems() {
        viewModelScope.launch {
            _items.value = Result.Loading
            try {
                val response = repository.fetchItems()
                if (response.isSuccessful) {
                    response.body()?.let { itemList ->
                        // Filter out items with blank or null names
                        val filtered = itemList.filter { !it.name.isNullOrBlank() }
                        // Group by listId and sort
                        val grouped = filtered.groupBy { it.listId }
                            .mapValues { entry ->
                                entry.value.sortedBy { it.name }
                            }
                        // Sort the map by listId
                        val sortedGrouped = grouped.toSortedMap()
                        _items.value = Result.Success(sortedGrouped)
                    } ?: run {
                        _items.value = Result.Error("No data available")
                    }
                } else {
                    _items.value = Result.Error("Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _items.value = Result.Error("Exception: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }
}

class ItemViewModelFactory(private val repository: ItemRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(ItemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ItemViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}