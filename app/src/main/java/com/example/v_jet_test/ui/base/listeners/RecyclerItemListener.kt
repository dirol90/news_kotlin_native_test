package com.example.v_jet_test.ui.base.listeners

import com.example.v_jet_test.data.dto.news.Article

interface RecyclerItemListener {
    fun onItemSelected(article: Article)
    fun onItemDownload(article: Article)
    fun onItemStarred(article: Article)
}
