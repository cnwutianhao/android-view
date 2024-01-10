package com.tyhoo.android.view.ui.skeleton

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.tyhoo.android.view.R
import com.tyhoo.android.view.widget.ShimmerLayout

class SkeletonAdapter :
    ListAdapter<String, SkeletonAdapter.SkeletonViewHolder>(SkeletonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkeletonViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_skeleton, parent, false)
        return SkeletonViewHolder(view)
    }

    override fun onBindViewHolder(holder: SkeletonViewHolder, position: Int) {
        holder.shimmerLayout.startShimmerAnimation()
    }

    inner class SkeletonViewHolder(view: View) : ViewHolder(view) {
        val shimmerLayout: ShimmerLayout = view.findViewById(R.id.shimmer_layout)
    }
}

private class SkeletonDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}