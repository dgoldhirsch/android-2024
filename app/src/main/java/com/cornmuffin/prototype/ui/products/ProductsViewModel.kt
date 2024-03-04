package com.cornmuffin.prototype.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cornmuffin.prototype.Navigator
import com.cornmuffin.prototype.data.products.Product
import com.cornmuffin.prototype.data.products.ProductsRepository
import com.cornmuffin.prototype.data.products.ProductsResponse
import com.cornmuffin.prototype.data.settings.SettingsRepository
import com.cornmuffin.prototype.ui.common.CannotGoBack
import com.cornmuffin.prototype.util.eventprocessor.EventProcessor
import com.cornmuffin.prototype.util.eventprocessor.EventQueue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repository: ProductsRepository,
    val settingsRepository: SettingsRepository,
    private val navigator: Navigator,
) : CannotGoBack, ViewModel() {
    private val _stateFlow = MutableStateFlow(ProductsViewModelState())
    internal fun stateFlow(): StateFlow<ProductsViewModelState> = _stateFlow.asStateFlow()

    sealed interface Event : EventQueue.Item {
        data class NavigateTo(val navTarget: Navigator.NavTarget) : Event
        data object ProductsUninitialized : Event
        data class ReceivedProducts(val productsResponse: ProductsResponse) : Event
        data class ReceivedRefreshedProducts(val productsResponse: ProductsResponse) : Event
        data object RefreshProducts : Event
        data object RetryProducts : Event

        data object SettingsUninitialized : Event {
            override fun isTopPriority(): Boolean = true
        }
    }

    private val eventProcessor = EventProcessor(
        scope = viewModelScope,
        control = { event: Event ->
            if (event is Event.SettingsUninitialized) {
                viewModelScope.launch { getSettings() }
            } else {
                when (stateFlow().value.state) {
                    ProductsViewModelState.State.UNINITIALIZED,
                    ProductsViewModelState.State.ERROR -> when (event) {
                        is Event.ProductsUninitialized,
                        is Event.RetryProducts -> {
                            reduceToLoading()
                            viewModelScope.launch { enqueue(Event.ReceivedProducts(getProducts())) }
                        }
                        else -> Unit
                    }

                    ProductsViewModelState.State.LOADING -> when (event) {
                        is Event.ReceivedProducts -> {
                            when (event.productsResponse) {
                                is ProductsResponse.Error -> { reduceToError(event.productsResponse.exception.message ?: "Bummer") }
                                is ProductsResponse.Success -> { reduceToSuccess(event.productsResponse.data) }
                                else -> Unit
                            }
                        }
                        else -> Unit
                    }

                    ProductsViewModelState.State.SUCCESSFUL -> when (event) {
                        is Event.RefreshProducts -> {
                            reduceToRefreshing()
                            viewModelScope.launch { refresh() }
                        }
                        is Event.NavigateTo -> { navigator.navigateTo(event.navTarget) }
                        else -> Unit
                    }

                    ProductsViewModelState.State.REFRESHING -> when (event) {
                        is Event.ReceivedRefreshedProducts -> {
                            when (event.productsResponse) {
                                is ProductsResponse.Error -> { reduceToError(event.productsResponse.exception.message ?: "Bummer") }
                                is ProductsResponse.Success -> { reduceToSuccess(event.productsResponse.data) }
                                else -> Unit
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
    )

    init {
        eventProcessor.enqueue(Event.SettingsUninitialized, Event.ProductsUninitialized)
    }

    /**
     * Public interface so that Layout can prod our state with an event.
     */
    fun enqueue(vararg events: Event) {
        eventProcessor.enqueue(*events)
    }

    private fun reduceToError(message: String) {
        _stateFlow.value = _stateFlow.value.asError(message)
    }

    private fun reduceToLoading() {
        _stateFlow.value = _stateFlow.value.asLoading()
    }

    private fun reduceToRefreshing() {
        _stateFlow.value = _stateFlow.value.asRefreshing()
    }

    private fun reduceToSuccess(products: ImmutableList<Product>) {
        _stateFlow.value = _stateFlow.value.asSuccess(products)
    }

    private suspend fun getProducts() = try {
        withContext(Dispatchers.IO) { repository.getProducts() }
    } catch (e: Exception) {
        ProductsResponse.Error(e)
    }

    private suspend fun getSettings() {
        withContext(Dispatchers.IO) { settingsRepository.initialize() }
    }

    private suspend fun refresh() {
        withContext(Dispatchers.IO) {
            repository.flushCache()
            eventProcessor.enqueue(Event.ReceivedRefreshedProducts(getProducts()))
        }
    }
}
