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


class ComicAdapter (private val context: Context, private val comicList: ArrayList<Comic>) :
    RecyclerView.Adapter<ComicAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover: ImageView = itemView.findViewById(R.id.ivCover)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvChapter: TextView = itemView.findViewById(R.id.tvChapter)
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

        ComicLoader.loadSrc(context, comic.src, holder.ivCover)
        holder.tvTitle.text = comic.title
        holder.tvChapter.text = comic.lastChapter

        holder.ivCover.setOnClickListener {
            val intent = Intent(context, CoverActivity::class.java)
            intent.putExtra("href", comic.href)
            intent.putExtra("title", comic.title)
            context.startActivity(intent)
        }
    }
}