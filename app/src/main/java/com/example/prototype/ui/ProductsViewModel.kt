package com.example.prototype.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototype.Product
import com.example.prototype.repositories.product.ProductsResponse
import com.example.prototype.repositories.product.ProductRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()
    private val repository = ProductRepository()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            repository.fetchNetworkResult
                .flowOn(Dispatchers.IO)
                .catch {
                    emit(ProductsResponse.Error(it))
                }
                .collect { productsResponse ->
                    when (productsResponse) {
                        is ProductsResponse.Success -> _uiState.update {
                            uiState.value.asSuccess(
                                productsResponse.data as? ImmutableList<Product> ?: persistentListOf()
                            )
                        }

                        is ProductsResponse.Loading -> _uiState.update { uiState.value.asLoading() }
                        is ProductsResponse.Error -> _uiState.update {
                            uiState.value.asError(
                                retryNumber = uiState.value.retryNumber + 1,
                                errorMessage = productsResponse.exception.message ?: "Bummer",
                            )
                        }
                    }
                }
        }
    }
}
