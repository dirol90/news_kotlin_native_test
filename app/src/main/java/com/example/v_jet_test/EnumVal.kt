package com.example.v_jet_test

class EnumVal {
    enum class SortStatus(private val value: String) {
        RELEVANCY("relevancy"),
        POPULARITY("popularity"),
        PUBLISHED_AT("publishedAt");

        override fun toString(): String {
            return value
        }
    }
}