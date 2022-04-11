package com.example.v_jet_test.data.dto.news

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Article(
    @SerializedName("author")
    val author: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("publishedAt")
    val publishedAt: String,
    @SerializedName("source")
    val source: Source,
    @SerializedName("title")
    val title: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("urlToImage")
    val urlToImage: String,
    var isSelected: Boolean
) : Parcelable {

    override fun toString(): String {
        return Gson().toJson(this)
    }

    companion object {
        fun fromString(str: String): Article {
            return Gson().fromJson(str, Article::class.java)
        }
    }
}