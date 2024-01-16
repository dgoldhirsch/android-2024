package com.example.prototype.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototype.Product
import com.example.prototype.repositories.NetworkResult
import com.example.prototype.repositories.product.ProductRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
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
                    emit(NetworkResult.Error(it))
                }
                .collect { networkResult ->
                    when (networkResult) {
                        is NetworkResult.Success<*> -> _uiState.update {
                            uiState.value.asSuccess(
                                networkResult.data as? ImmutableList<Product> ?: persistentListOf()
                            )
                        }

                        is NetworkResult.Loading -> _uiState.update { uiState.value.asLoading() }
                        is NetworkResult.Error -> _uiState.update {
                            uiState.value.asError(
                                retryNumber = uiState.value.retryNumber + 1,
                                errorMessage = networkResult.exception.message ?: "Bummer",
                            )
                        }
                    }
                }
        }
    }
}
