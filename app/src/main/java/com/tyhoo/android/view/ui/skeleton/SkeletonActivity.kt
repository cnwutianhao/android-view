package com.tyhoo.android.view.ui.skeleton

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.tyhoo.android.view.R
import com.tyhoo.android.view.api.ApiService
import com.tyhoo.android.view.data.Article
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SkeletonActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skeleton)

        val articlesView = findViewById<RecyclerView>(R.id.article_list)
        articlesView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val list = MutableList(6) { "" }
        val skeletonAdapter = SkeletonAdapter()
        articlesView.adapter = skeletonAdapter
        skeletonAdapter.submitList(list)

        val articleAdapter = ArticleAdapter(this, object : ArticleAdapter.OnItemClickListener {
            override fun onItemClick(article: Article) {
                val intent = Intent(this@SkeletonActivity, SkeletonDetailActivity::class.java)
                intent.putExtra("articleId", article.id)
                startActivity(intent)
            }
        })

        val retrofit = Retrofit.Builder()
            .baseUrl("https://dongqiudi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        MainScope().launch {
            val response = apiService.articles()
            if (response.isSuccessful) {
                Log.d(TAG, "网络请求成功")
                delay(2000)

                val data = response.body()
                data?.let { hot ->
                    val articles = hot.articles

                    articlesView.adapter = articleAdapter
                    articleAdapter.submitList(articles)
                }
            } else {
                Log.d(TAG, "网络请求失败")
            }
        }
    }

    companion object {
        private const val TAG = "Tyhoo_Skeleton"
    }
}