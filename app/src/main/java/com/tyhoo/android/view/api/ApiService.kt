package com.tyhoo.android.view.api

import com.tyhoo.android.view.data.Detail
import com.tyhoo.android.view.data.Hot
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("api/app/tabs/web/1.json?size=50")
    suspend fun articles(): Response<Hot>

    @GET("v2/article/detail/{articleId}?isDarkMode=0&isTeenager=0&lang=zh-cn&platform=iphone&version=809")
    suspend fun article(@Path("articleId") articleId: String): Response<Detail>
}