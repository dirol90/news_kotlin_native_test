package com.example.v_jet_test.data.error

import javax.inject.Inject

class NetworkError @Inject constructor(val code: Int, val description: String) {
    constructor(exception: Exception) : this(
        code = DEFAULT_ERROR,
        description = exception.message ?: ""
    )
}

const val NO_INTERNET_CONNECTION = -101
const val NETWORK_ERROR = -201
const val DEFAULT_ERROR = -301
