package com.manhua.oh.adapter

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SimpleAdapter
import android.widget.TextView
import com.manhua.oh.R
import com.manhua.oh.activity.CoverActivity
import com.manhua.oh.tool.ComicLoader

class SearchSimpleAdapter(
        private val context: Context,
        private val data: MutableList<out MutableMap<String, String>>,
        resource: Int,
        from: Array<out String>?,
        to: IntArray?
) : SimpleAdapter(context, data, resource, from, to) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = super.getView(position, convertView, parent)
        val hm = data[position]
        view.setOnClickListener {
            context.startActivity(Intent(context, CoverActivity::class.java)
                    .putExtra("href", hm["href"])
                    .putExtra("title", hm["tvTitle"])
            )
        }

        val ivCover = view.findViewById<ImageView>(R.id.ivCover)
        ComicLoader.loadSrc(context, hm["src"]!!, ivCover)

        val tags = hm["tags"]!!.split(",")
        var count = 0
        for (tag in tags) {
            var tvType: TextView? = null
            if (count == 0) {
                tvType = view.findViewById(R.id.tvType1)
            }
            if (count == 1) {
                tvType = view.findViewById(R.id.tvType2)
            }
            if (count == 2) {
                tvType = view.findViewById(R.id.tvType3)
            }
            if (count == 3) {
                tvType = view.findViewById(R.id.tvType4)
            }
            count++

            if (tvType != null && tag.isNotEmpty()) {
                tvType.text = tag
                tvType.visibility = View.VISIBLE
            }
        }

        return view as View
    }
}