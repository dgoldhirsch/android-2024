package com.cornmuffin.prototype.data.settings

import javax.inject.Singleton

@Singleton
data class Settings(
    val isInitialized: Boolean = false,
    val enableDebugging: Boolean = false,
)
