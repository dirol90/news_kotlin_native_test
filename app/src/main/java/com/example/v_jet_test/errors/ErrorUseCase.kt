package com.example.v_jet_test.errors

import com.example.v_jet_test.data.error.NetworkError

interface ErrorUseCase {
    fun getError(errorCode: Int): NetworkError
}
