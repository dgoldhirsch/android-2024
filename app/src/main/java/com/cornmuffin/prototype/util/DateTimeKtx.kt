package com.cornmuffin.prototype.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

fun dawnOfZonedDateTime(): ZonedDateTime = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault())
