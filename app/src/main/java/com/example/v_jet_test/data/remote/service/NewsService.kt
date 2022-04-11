package com.example.v_jet_test.data.remote.service

import com.example.v_jet_test.EnumVal
import com.example.v_jet_test.data.dto.news.ArticleResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsService {

    /**
     * Date ISO 8601 format (e.g. 2022-04-09 or 2022-04-09T16:47:43)
     * **/

    @GET("/v2/everything")
    suspend fun fetchNews(
        @Query("q") question: String?,
        @Query("from") fromDate: String?,
        @Query("to") toDate: String?,
        @Query("sortBy") sortBy: EnumVal.SortStatus?,
        @Query("pageSize") pageSize: Int?,
        @Query("page") page: Int?
    ): Response<ArticleResponse>
}
