package com.tyhoo.android.view.data

data class Hot(
    val label: String,
    val articles: List<Article>
)

data class Article(
    val id: Int,
    val title: String,
    val thumb: String,
    val category: String
)

data class Detail(
    val data: DetailData
)

data class DetailData(
    val title: String,
    val body: String,
    val thumb: String
)