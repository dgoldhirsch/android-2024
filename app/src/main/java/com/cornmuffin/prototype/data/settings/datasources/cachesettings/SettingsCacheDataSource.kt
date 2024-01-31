package com.cornmuffin.prototype.data.settings.datasources.cachesettings

import com.cornmuffin.prototype.data.room.Database
import javax.inject.Inject

class SettingsCacheDataSource @Inject constructor(
    private val database: Database,
) {
    fun binarySettings() = database.binarySettings()
}
