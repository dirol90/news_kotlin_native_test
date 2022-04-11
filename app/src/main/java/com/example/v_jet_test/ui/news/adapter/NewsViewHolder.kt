package com.example.v_jet_test.ui.news.adapter

import android.os.Build
import android.text.Html
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.v_jet_test.data.dto.news.Article
import com.example.v_jet_test.databinding.RecipeItemBinding
import com.example.v_jet_test.ui.base.listeners.RecyclerItemListener

class NewsViewHolder(private val itemBinding: RecipeItemBinding) :
    RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(article: Article, recyclerItemListener: RecyclerItemListener) {
        itemBinding.tvCaption.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(article.description, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(article.description)
        }
        itemBinding.tvName.text = article.author
        itemBinding.rlNewsItem.setOnClickListener { recyclerItemListener.onItemSelected(article) }
        itemBinding.ibDownload.setOnClickListener { recyclerItemListener.onItemDownload(article) }
        itemBinding.ibFavorite.setOnClickListener {
            recyclerItemListener.onItemStarred(article)
            updateStar(article)
        }

        Glide.with(itemView.context).load(article.urlToImage).placeholder(AppCompatResources.getDrawable(itemBinding.root.rootView.context, android.R.drawable.ic_menu_myplaces))
            .diskCacheStrategy(DiskCacheStrategy.DATA).into(itemBinding.iv)
        updateStar(article)
    }

    private fun updateStar(article: Article) {
        itemBinding.ibFavorite.setImageDrawable(
            if (!article.isSelected) AppCompatResources.getDrawable(itemBinding.root.rootView.context, android.R.drawable.btn_star_big_off)
            else AppCompatResources.getDrawable(itemBinding.root.rootView.context, android.R.drawable.btn_star_big_on)
        )
    }
}

