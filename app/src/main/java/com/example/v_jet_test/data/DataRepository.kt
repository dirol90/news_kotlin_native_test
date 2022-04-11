package com.example.v_jet_test.data

import android.content.Context
import com.example.v_jet_test.EnumVal
import com.example.v_jet_test.data.dto.news.Article
import com.example.v_jet_test.data.dto.news.ArticleResponse
import com.example.v_jet_test.data.local.LocalData
import com.example.v_jet_test.data.remote.RemoteData
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
class DataRepository @Inject constructor(
    private val remoteRepository: RemoteData,
    private val localRepository: LocalData
) {

    suspend fun requestNews(
        question: String?,
        fromDate: String?,
        toDate: String?,
        sortBy: EnumVal.SortStatus?,
        pageSize: Int? = 20,
        page: Int? = 0
    ): Flow<Resource<ArticleResponse>> {
        return flow {
            emit(remoteRepository.requestNews(question, fromDate, toDate, sortBy, pageSize, page))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getAllFavorites(@ApplicationContext context: Context): Flow<Resource<MutableList<String>>> {
        return flow {
            emit(localRepository.getAllCachedFavourites(context))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun addToFavourite(
        id: String,
        article: Article,
        @ApplicationContext context: Context
    ): Flow<Resource<Boolean>> {
        return flow {
            localRepository.isFavourite(id, context).let {
                it.data?.let {
                    val isAdded = it
                    if (!isAdded) {
                        emit(localRepository.cacheFavourites(id, article.toString(), context))
                    } else {
                        emit(localRepository.removeFromFavourites(id, context))
                    }
                }
                it.errorCode?.let { errorCode ->
                    emit(Resource.DataError<Boolean>(errorCode))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun isFavourite(
        id: String,
        @ApplicationContext context: Context
    ): Flow<Resource<Boolean>> {
        return flow {
            emit(localRepository.isFavourite(id, context))
        }.flowOn(Dispatchers.Main)
    }
}
