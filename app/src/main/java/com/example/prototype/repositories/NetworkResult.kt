package com.example.prototype.repositories

sealed class NetworkResult {
    class Loading : NetworkResult()
    data class Success<PARSED_DATA_TYPE>(val data: PARSED_DATA_TYPE) : NetworkResult()
    class Error(val exception: Throwable = NoException) : NetworkResult()
}

object NoException : Exception()
