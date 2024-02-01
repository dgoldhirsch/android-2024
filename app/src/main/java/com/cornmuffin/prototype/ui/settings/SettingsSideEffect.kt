package com.cornmuffin.prototype.ui.settings

import com.cornmuffin.prototype.data.settings.Settings

internal sealed interface SettingsSideEffect {
    data object Load : SettingsSideEffect
    data class WriteSettingsToDisk(val newSettings: Settings) : SettingsSideEffect
}
