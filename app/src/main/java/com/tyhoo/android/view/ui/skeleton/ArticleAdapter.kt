package com.tyhoo.android.view.ui.skeleton

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.tyhoo.android.view.R
import com.tyhoo.android.view.data.Article

class ArticleAdapter(
    private val context: Context,
    private val onItemClickListener: OnItemClickListener
) : ListAdapter<Article, ArticleAdapter.ArticleViewHolder>(ArticleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val item = getItem(position)

        Glide.with(context).load(item.thumb).into(holder.articleImg)
        holder.articleTitle.text = item.title
        holder.articleCategory.text =
            context.getString(R.string.skeleton_article_category, item.category)

        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(item)
        }
    }

    inner class ArticleViewHolder(view: View) : ViewHolder(view) {
        val articleImg: ImageView = view.findViewById(R.id.article_img)
        val articleTitle: TextView = view.findViewById(R.id.article_title)
        val articleCategory: TextView = view.findViewById(R.id.article_category)
    }

    interface OnItemClickListener {
        fun onItemClick(article: Article)
    }
}

private class ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem == newItem
    }
}