package com.manhua.oh.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.manhua.oh.R
import com.manhua.oh.activity.CoverActivity
import com.manhua.oh.bean.Comic
import com.manhua.oh.tool.ComicLoader


class ComicAdapter : RecyclerView.Adapter<ComicAdapter.ViewHolder> {
    var context: Context
    var comicList: List<Comic>

    constructor(context: Context, comicList: ArrayList<Comic>) {
        this.context = context
        this.comicList = comicList
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvVote: TextView = itemView.findViewById(R.id.tvVote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_recyclerview_comic, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return comicList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comic = comicList[position]

        ComicLoader.loadSrc(context, comic.src, holder.ivPicture)
//        holder.rbRate.rating = comic.rate
        holder.tvTitle.text = comic.title
        holder.tvVote.text = comic.lastChapter

        holder.ivPicture.setOnClickListener {
            val intent = Intent(context, CoverActivity::class.java)
            intent.putExtra("href", comic.href)
            intent.putExtra("title", comic.title)
            context.startActivity(intent)
        }
    }
}