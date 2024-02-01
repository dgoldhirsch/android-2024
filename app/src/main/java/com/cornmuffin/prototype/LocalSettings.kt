package com.cornmuffin.prototype

import androidx.compose.runtime.compositionLocalOf
import com.cornmuffin.prototype.data.settings.Settings

val LocalSettings = compositionLocalOf<Settings> { error("No Settings for LocalSettings provider?!") }
