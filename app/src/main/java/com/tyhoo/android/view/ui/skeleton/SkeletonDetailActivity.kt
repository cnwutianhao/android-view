package com.tyhoo.android.view.ui.skeleton

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.bumptech.glide.Glide
import com.tyhoo.android.view.R
import com.tyhoo.android.view.api.ApiService
import com.tyhoo.android.view.widget.ShimmerLayout
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SkeletonDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skeleton_detail)

        val articleId = intent.getIntExtra("articleId", 0)

        val skeletonLayout = findViewById<ShimmerLayout>(R.id.skeleton_detail_sl)
        val detailLayout = findViewById<NestedScrollView>(R.id.skeleton_detail_nsv)
        val title = findViewById<TextView>(R.id.skeleton_detail_title)
        val image = findViewById<ImageView>(R.id.skeleton_detail_image)
        val webView = findViewById<WebView>(R.id.skeleton_detail_webView)

        skeletonLayout.visibility = View.VISIBLE
        detailLayout.visibility = View.GONE
        skeletonLayout.startShimmerAnimation()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://detail.dongqiudi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        MainScope().launch {
            val response = apiService.article(articleId.toString())
            if (response.isSuccessful) {
                Log.d(TAG, "网络请求成功")
                delay(2000)

                skeletonLayout.stopShimmerAnimation()
                skeletonLayout.visibility = View.GONE
                detailLayout.visibility = View.VISIBLE

                val data = response.body()
                data?.let { detail ->
                    title.text = detail.data.title
                    val imageUrl = detail.data.thumb
                    Glide.with(this@SkeletonDetailActivity).load(imageUrl).into(image)
                    val htmlData = detail.data.body
                    webView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null)
                }
            } else {
                Log.d(TAG, "网络请求失败")
            }
        }
    }

    companion object {
        private const val TAG = "Tyhoo_Skeleton_Detail"
    }
}