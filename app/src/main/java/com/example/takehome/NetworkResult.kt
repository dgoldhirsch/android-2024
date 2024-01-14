package com.example.takehome

sealed class NetworkResult<T> {
    class Loading<T> : NetworkResult<T>()
    data class Success<T>(val data: T) : NetworkResult<T>()
    class Error<T>(val exception: Throwable = NoException) : NetworkResult<T>()
}

object NoException : Exception()
