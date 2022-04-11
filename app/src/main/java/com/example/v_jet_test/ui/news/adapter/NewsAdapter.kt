package com.example.v_jet_test.ui.news.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.v_jet_test.data.dto.news.Article
import com.example.v_jet_test.databinding.RecipeItemBinding
import com.example.v_jet_test.ui.base.listeners.RecyclerItemListener
import com.example.v_jet_test.ui.news.NewsListViewModel

class NewsAdapter(
    private val newsListViewModel: NewsListViewModel,
    private var news: MutableList<Article>
) : RecyclerView.Adapter<NewsViewHolder>() {
    private var context: Context? = null

    private val onItemClickListener: RecyclerItemListener = object : RecyclerItemListener {
        override fun onItemSelected(article: Article) {
            newsListViewModel.openNewsDetails(article)
        }

        override fun onItemDownload(article: Article) {
            newsListViewModel.downloadImage(article)
        }

        override fun onItemStarred(article: Article) {
            article.isSelected = !article.isSelected
            context?.let { newsListViewModel.addToFavorite(article, it) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val itemBinding =
            RecipeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        context = holder.itemView.context
        holder.bind(news[position], onItemClickListener)
        newsListViewModel.itemIndex(position, context!!)
    }

    override fun getItemCount(): Int {
        return news.size
    }

    fun addItems(newItems: MutableList<Article>) {
        val sortedItems: MutableList<Article> = mutableListOf()
        for (article in newItems) {
            if (!news.contains(article)) {
                sortedItems.add(article)
            }
        }
        news.addAll(sortedItems)
        notifyDataSetChanged()
    }

    fun clear() {
        news.clear()
    }
}

