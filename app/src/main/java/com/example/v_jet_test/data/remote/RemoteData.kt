package com.example.v_jet_test.data.remote

import com.example.v_jet_test.EnumVal
import com.example.v_jet_test.data.Resource
import com.example.v_jet_test.data.dto.news.ArticleResponse
import com.example.v_jet_test.data.error.NETWORK_ERROR
import com.example.v_jet_test.data.error.NO_INTERNET_CONNECTION
import com.example.v_jet_test.data.remote.service.NewsService
import com.example.v_jet_test.utils.Network
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class RemoteData @Inject
constructor(
    private val serviceGenerator: ServiceGenerator,
    private val networkConnectivity: Network
) :
    RemoteDataSource {
    override suspend fun requestNews(
        question: String?,
        fromDate: String?,
        toDate: String?,
        sortBy: EnumVal.SortStatus?,
        pageSize: Int?,
        page: Int?
    ): Resource<ArticleResponse> {
        val newsService = serviceGenerator.createService(NewsService::class.java)
        return when (val response = processCall {
            newsService.fetchNews(
                question,
                fromDate,
                toDate,
                sortBy,
                pageSize,
                page
            )
        }) {
            is ArticleResponse -> {
                Resource.Success(
                    data = response
                )
            }
            else -> {
                Resource.DataError(errorCode = response as Int)
            }
        }
    }

    private suspend fun processCall(responseCall: suspend () -> Response<*>): Any? {
        if (!networkConnectivity.isConnected()) {
            return NO_INTERNET_CONNECTION
        }
        return try {
            val response = responseCall.invoke()
            val responseCode = response.code()
            if (response.isSuccessful) {
                response.body()
            } else {
                responseCode
            }
        } catch (e: IOException) {
            NETWORK_ERROR
        }
    }
}
