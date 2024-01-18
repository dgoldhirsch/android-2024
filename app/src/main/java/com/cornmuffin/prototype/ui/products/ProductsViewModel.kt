package com.cornmuffin.prototype.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cornmuffin.prototype.data.products.ProductsResponse
import com.cornmuffin.prototype.data.products.ProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repository: ProductsRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            repository.fetchProductsResponse
                .flowOn(Dispatchers.IO)
                .catch {
                    emit(ProductsResponse.Error(it))
                }
                .collect { productsResponse -> reduceLoadedProducts(productsResponse) }
        }
    }

    private fun reduceLoadedProducts(productsResponse: ProductsResponse) {
        when (productsResponse) {
            is ProductsResponse.Success -> _uiState.update { uiState.value.asSuccess(productsResponse.data) }
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