package com.cornmuffin.prototype.data.settings

import com.cornmuffin.prototype.data.settings.datasources.cachesettings.SettingsCacheDataSource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject internal constructor(
    cacheDataSource: SettingsCacheDataSource,
) {
    val settings: Flow<ImmutableList<Setting>> = flow {
        emit(cacheDataSource.binarySettings())
    }
}
