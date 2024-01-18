package com.example.prototype.ui

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class Navigator {
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
