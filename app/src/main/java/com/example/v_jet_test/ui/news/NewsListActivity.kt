package com.example.v_jet_test.ui.news

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.v_jet_test.EnumVal
import com.example.v_jet_test.R
import com.example.v_jet_test.data.Resource
import com.example.v_jet_test.data.dto.news.Article
import com.example.v_jet_test.data.dto.news.ArticleResponse
import com.example.v_jet_test.databinding.HomeActivityBinding
import com.example.v_jet_test.ui.base.BaseActivity
import com.example.v_jet_test.ui.news.adapter.NewsAdapter
import com.example.v_jet_test.utils.SingleEvent
import com.example.v_jet_test.utils.observe
import com.example.v_jet_test.utils.observeEvent
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.make
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class NewsListActivity : BaseActivity() {
    private lateinit var binding: HomeActivityBinding
    private val newsListViewModel: NewsListViewModel by viewModels()
    private var newsAdapter: NewsAdapter? = null

    override fun initViewBinding() {
        binding = HomeActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = getString(R.string.news)
        val layoutManager = LinearLayoutManager(this)
        binding.rvList.layoutManager = layoutManager
        binding.rvList.setHasFixedSize(false)
        newsListViewModel.getNews(context = this)
        binding.ibStarredList.setOnClickListener { newsListViewModel.getStarredNews(this) }
        fillSpinner()
        listenDates()
    }

    private fun fillSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, EnumVal.SortStatus.values()
        )
        binding.spValue.adapter = adapter

        binding.spValue.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                newsAdapter?.clear()
                newsListViewModel.getNews(
                    sortBy = EnumVal.SortStatus.values()[position],
                    context = baseContext
                )
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun listenDates() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        binding.dateFrom.setOnClickListener {
            DatePickerDialog(this, { _, yearOfYear, monthOfYear, dayOfMonth ->
                val date = "$dayOfMonth.${monthOfYear + 1}.$yearOfYear"
                binding.dateFrom.setText(date)
                newsListViewModel.getNews(dateFrom = date, context = this)
            }, year, month, day).show()
        }

        binding.dateTo.setOnClickListener {
            DatePickerDialog(this, { _, yearOfYear, monthOfYear, dayOfMonth ->
                val date = "$dayOfMonth.${monthOfYear + 1}.$yearOfYear"
                binding.dateTo.setText(date)
                newsListViewModel.getNews(dateTo = date, context = this)
            }, year, month, day).show()
        }
    }

    private fun bindListData(articles: MutableList<Article>) {
        if (newsAdapter == null) {
            newsAdapter = NewsAdapter(newsListViewModel, articles)
            binding.rvList.adapter = newsAdapter
        } else {
            newsAdapter!!.addItems(newsListViewModel.getData())
        }

        showDataView(true)
    }

    private fun navigateToDetailsScreen(navigateEvent: SingleEvent<Article>) {
        navigateEvent.getContentIfNotHandled()?.let { article ->
            startActivity(Intent.createChooser(Intent().also { intent ->
                intent.action = Intent.ACTION_SEND
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, "${article.content}: ${article.url}")
            }, "Share with:"))
        }
    }

    private fun downloadImage(navigateEvent: SingleEvent<Article>) {
        navigateEvent.getContentIfNotHandled()?.let { article ->
            newsListViewModel.saveMediaToStorage(article, this)
        }
    }

    private fun observeSnackBarMessages(event: SingleEvent<Any>) {
        event.getContentIfNotHandled().let {
            make(binding.root, it.toString(), Snackbar.LENGTH_LONG).show()
        }
    }

    private fun observeStarredData(mutableList: MutableList<Article>) {
        showDataView(false)
        newsAdapter?.clear()
        bindListData(articles = mutableList)
    }

    private fun observeNewsData(status: Resource<ArticleResponse>) {
        when (status) {
            is Resource.Loading -> showLoadingView()
            is Resource.Success -> status.data?.let {
                showDataView(false)
                bindListData(articles = it.articles)
            }
            is Resource.DataError -> {
                showDataView(newsListViewModel.getData().isNotEmpty())
                status.errorCode?.let {
                    newsListViewModel.showSnackBar(it)
                }
            }
        }
    }

    private fun showDataView(show: Boolean) {
        binding.tvNoData.visibility = if (show) GONE else VISIBLE
        binding.pbLoading.visibility = GONE
    }

    private fun showLoadingView() {
        binding.pbLoading.visibility = VISIBLE
        binding.tvNoData.visibility = GONE
    }

    override fun observeViewModel() {
        observe(newsListViewModel.newsLiveData, ::observeNewsData)
        observe(newsListViewModel.starredLiveData, ::observeStarredData)
        observeEvent(newsListViewModel.openNewsDetails, ::navigateToDetailsScreen)
        observeEvent(newsListViewModel.downloadDetails, ::downloadImage)
        observeEvent(newsListViewModel.showSnackBar, ::observeSnackBarMessages)
    }
}
