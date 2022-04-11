package com.example.v_jet_test.errors

import com.example.v_jet_test.data.error.mapper.ErrorMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
class ErrorManager @Inject constructor(private val errorMapper: ErrorMapper) : ErrorUseCase {

    override fun getError(errorCode: Int): com.example.v_jet_test.data.error.NetworkError {
        return com.example.v_jet_test.data.error.NetworkError(
            code = errorCode,
            description = errorMapper.errorsMap.getValue(errorCode)
        )
    }
}
