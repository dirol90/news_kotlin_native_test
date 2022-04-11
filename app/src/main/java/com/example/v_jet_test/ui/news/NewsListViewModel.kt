package com.example.v_jet_test.ui.news

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.v_jet_test.EnumVal
import com.example.v_jet_test.data.DataRepository
import com.example.v_jet_test.data.Resource
import com.example.v_jet_test.data.dto.news.Article
import com.example.v_jet_test.data.dto.news.ArticleResponse
import com.example.v_jet_test.ui.base.BaseViewModel
import com.example.v_jet_test.utils.SingleEvent
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class NewsListViewModel @Inject constructor(private val dataRepositoryRepository: DataRepository) :
    BaseViewModel() {

    private val requestSize = 50
    private var lastRequestedPage = 1
    private var newsList = mutableListOf<Article>()
    private var prevSortOrder: EnumVal.SortStatus? = null
    private var prevDateFrom: String? = null
    private var prevDateTo: String? = null

    private val newsLiveDataPrivate = MutableLiveData<Resource<ArticleResponse>>()
    val newsLiveData: LiveData<Resource<ArticleResponse>> get() = newsLiveDataPrivate

    private val starredLiveDataPrivate = MutableLiveData<MutableList<Article>>()
    val starredLiveData: LiveData<MutableList<Article>> get() = starredLiveDataPrivate

    private val openNewsDetailsPrivate = MutableLiveData<SingleEvent<Article>>()
    val openNewsDetails: LiveData<SingleEvent<Article>> get() = openNewsDetailsPrivate

    private val downloadDetailsPrivate = MutableLiveData<SingleEvent<Article>>()
    val downloadDetails: LiveData<SingleEvent<Article>> get() = downloadDetailsPrivate

    private val showSnackBarPrivate = MutableLiveData<SingleEvent<Any>>()
    val showSnackBar: LiveData<SingleEvent<Any>> get() = showSnackBarPrivate

    fun showSnackBar(any: Any) {
        showSnackBarPrivate.value = SingleEvent(any)
    }

    fun saveMediaToStorage(article: Article, @ApplicationContext context: Context) {
        showSnackBar("Start downloading  ...")
        CoroutineScope(IO).launch {
            run {
                val imageDownloader = ImageLoader.getInstance()
                imageDownloader.init(ImageLoaderConfiguration.createDefault(context))
                imageDownloader.loadImage(
                    article.urlToImage,
                    object : SimpleImageLoadingListener() {
                        override fun onLoadingComplete(
                            imgPath: String?,
                            view: View?,
                            loadedImage: Bitmap?
                        ): Unit = loadedImage.let { bitmap ->
                            val filename = "${System.currentTimeMillis()}.jpg"
                            var fos: OutputStream?
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                context.contentResolver.also { resolver ->
                                    val contentValues = ContentValues().apply {
                                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                                        put(
                                            MediaStore.MediaColumns.RELATIVE_PATH,
                                            Environment.DIRECTORY_PICTURES
                                        )
                                    }
                                    val imageUri: Uri? =
                                        resolver.insert(
                                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                            contentValues
                                        )
                                    fos = imageUri?.let { resolver.openOutputStream(it) }
                                }
                            } else {
                                val imagesDir =
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                val image = File(imagesDir, filename)
                                fos = FileOutputStream(image)
                            }

                            fos?.use {
                                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, it)
                            }
                            CoroutineScope(Main).launch {
                                showSnackBar("Downloaded ...")
                            }
                        }
                    })
            }
        }
    }

    fun getNews(
        dateFrom: String? = null,
        dateTo: String? = null,
        sortBy: EnumVal.SortStatus? = null,
        page: Int = lastRequestedPage,
        @ApplicationContext context: Context
    ) {
        if (sortBy != null && sortBy != prevSortOrder) {
            prevSortOrder = sortBy
            cleanValues()
        }

        if (dateTo != null && dateTo != prevDateTo) {
            prevDateTo = dateTo
            cleanValues()
        }

        if (dateFrom != null && dateFrom != prevDateFrom) {
            prevDateFrom = dateFrom
            cleanValues()
        }

        viewModelScope.launch {
            newsLiveDataPrivate.value = Resource.Loading()
            dataRepositoryRepository.requestNews(
                "news",
                parseTime(prevDateFrom),
                parseTime(prevDateTo),
                prevSortOrder,
                requestSize,
                page
            )
                .collect {
                    it.data?.articles?.let { articles ->
                        for (art in articles) {
                            setFavoriteState(art, context)
                        }
                        newsList.addAll(articles)
                    }
                    newsLiveDataPrivate.value = it
                }
        }
    }

    fun getStarredNews(@ApplicationContext context: Context){
        newsList.clear()
        viewModelScope.launch {
            newsLiveDataPrivate.value = Resource.Loading()
            dataRepositoryRepository.getAllFavorites(context)
                .collect {
                    it.data?.let { articles ->
                        for (art in articles) {
                            val article = Article.fromString(art)
                            article.isSelected = true
                            newsList.add(article)
                        }
                    }
                    starredLiveDataPrivate.value = newsList
                }
        }
    }

    fun addToFavorite(article: Article, @ApplicationContext context: Context) {
        viewModelScope.launch {
            dataRepositoryRepository.addToFavourite(article.url, article, context).collect()
        }
    }

    fun getData(): MutableList<Article> {
        return newsList
    }

    fun openNewsDetails(article: Article) {
        openNewsDetailsPrivate.value = SingleEvent(article)
    }

    fun downloadImage(article: Article) {
        downloadDetailsPrivate.value = SingleEvent(article)
    }

    fun itemIndex(index: Int, @ApplicationContext context: Context) {
        if (index + requestSize < newsLiveDataPrivate.value?.data?.totalResults ?: 0 &&
            index + requestSize / 2 == newsList.size
        ) {
            getNews(page = ++lastRequestedPage, context = context)
        }
    }

    private fun setFavoriteState(article: Article, @ApplicationContext context: Context) {
        viewModelScope.launch {
            dataRepositoryRepository.isFavourite(article.url, context).collect {
                article.isSelected =  it.data ?: false
            }
        }
    }

    private fun cleanValues() {
        newsList.clear()
        lastRequestedPage = 1
    }

    private fun parseTime(time: String?): String? {
        if (time == null) return null
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.UK)
        return sdf.format(
            Date(
                time.replaceBeforeLast(".", "").replace(".", "").toInt(),
                time.replaceBefore(".", "").replaceAfterLast(".", "").replace(".", "").toInt(),
                time.replaceAfter(".", "").replace(".", "").toInt(),
            )
        )
    }
}


