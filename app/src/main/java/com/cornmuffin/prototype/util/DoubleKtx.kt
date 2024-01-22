package com.cornmuffin.prototype.util

import kotlin.math.roundToInt

fun Double.roundToNearestHalf() = (this * 2).roundToInt() / 2.0
