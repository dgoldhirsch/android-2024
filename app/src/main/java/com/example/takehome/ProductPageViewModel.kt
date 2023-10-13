package com.example.takehome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class ProductPageViewModel : ViewModel() {
    private val repository = ProductRepository()
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    fun fetchProducts() {
        viewModelScope.launch {
            repository.productFlow
                .flowOn(Dispatchers.IO)
                .catch { e -> println(">>>>> FETCH ERROR ${e.message}") }
                .collect {
                    println(">>>>> COLLECTING...")
                    when (it) {
                        is NetworkResult.Success -> _products.value = it.data
                        is NetworkResult.Loading -> _products.value = listOf(Product(name = "Loading..."))
                        is NetworkResult.Error -> _products.value = listOf(Product(name = "Error: ${it.exception.message}"))
                    }
                }
        }
    }
}
