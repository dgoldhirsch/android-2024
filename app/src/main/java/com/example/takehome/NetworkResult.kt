package com.example.takehome

sealed class NetworkResult<T>(
    val data: T? = null,
    val exception: Exception = NoException,
) {
    class Loading<T> : NetworkResult<T>()
    class Success<T>(data: T) : NetworkResult<T>(data)
    class Error<T>(exception: Exception, data: T? = null) : NetworkResult<T>(data, exception = exception)
}

object NoException : Exception()
