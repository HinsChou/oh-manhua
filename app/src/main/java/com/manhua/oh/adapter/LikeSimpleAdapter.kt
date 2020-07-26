package com.manhua.oh.adapter

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SimpleAdapter
import com.manhua.oh.R
import com.manhua.oh.activity.CoverActivity
import com.manhua.oh.tool.ComicLoader

class LikeSimpleAdapter(
        private val context: Context,
        private val data: MutableList<out MutableMap<String, String>>,
        resource: Int,
        from: Array<out String>?,
        to: IntArray?
) : SimpleAdapter(context, data, resource, from, to) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = super.getView(position, convertView, parent)

        val hm = data[position]
        val href = hm["href"]

        val src = hm["src"]
        val imageView = view.findViewById<ImageView>(R.id.ivCover)
        ComicLoader.loadSrc(context, src!!, imageView)

        view.setOnClickListener {
            val intent = Intent(context, CoverActivity::class.java)
            intent.putExtra("href", href)
            intent.putExtra("title", hm["tvName"])
            context.startActivity(intent)
        }

        return view as View
    }
}