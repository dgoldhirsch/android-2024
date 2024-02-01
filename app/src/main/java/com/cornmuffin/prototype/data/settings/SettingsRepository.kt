package com.cornmuffin.prototype.data.settings

import com.cornmuffin.prototype.data.settings.datasources.cachesettings.SettingsCacheDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject internal constructor(
    private val cacheDataSource: SettingsCacheDataSource,
) {
    private val _settings: MutableStateFlow<Settings> = MutableStateFlow(Settings())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    suspend fun initialize() {
        coroutineScope {
            launch(Dispatchers.IO) {
                _settings.value = cacheDataSource.settings()
            }
        }
    }

    suspend fun replaceSettings(newSettings: Settings) {
        _settings.value = newSettings

        coroutineScope {
            launch(Dispatchers.IO) {
                cacheDataSource.replaceSettings(newSettings)
                // TODO If error, roll back _settings value
            }
        }
    }
}
