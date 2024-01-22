package com.cornmuffin.prototype.util

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

fun <T> ImmutableList<T>.copy(): ImmutableList<T> = toMutableList().toPersistentList()
