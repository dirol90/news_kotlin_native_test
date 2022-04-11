package com.example.v_jet_test.data.remote

import com.example.v_jet_test.EnumVal
import com.example.v_jet_test.data.Resource
import com.example.v_jet_test.data.dto.news.ArticleResponse

internal interface RemoteDataSource {
    suspend fun requestNews(
        question: String?,
        fromDate: String?,
        toDate: String?,
        sortBy: EnumVal.SortStatus?,
        pageSize: Int?,
        page: Int?
    ): Resource<ArticleResponse>
}
