package com.cornmuffin.prototype

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Navigator @Inject constructor () {
    private val _sharedFlow = MutableSharedFlow<NavTarget>(extraBufferCapacity = 1)
    val sharedFlow = _sharedFlow.asSharedFlow()

    // Use the following once we have more than the single products page
//    fun navigateTo(navTarget: NavTarget) {
//        _sharedFlow.tryEmit(navTarget)
//    }

    enum class NavTarget(val label: String) {
        Products("products")
    }
}
