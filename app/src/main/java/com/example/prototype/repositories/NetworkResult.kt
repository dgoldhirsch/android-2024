package com.example.prototype.repositories

sealed class NetworkResult {
    class Loading : NetworkResult()
    data class Success<T>(val data: T) : NetworkResult()
    class Error(val exception: Throwable = NoException) : NetworkResult()
}

object NoException : Exception()
