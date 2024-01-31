package com.cornmuffin.prototype.ui.settings

internal sealed interface SettingsSideEffect {
    data object FetchForLoad : SettingsSideEffect
}
