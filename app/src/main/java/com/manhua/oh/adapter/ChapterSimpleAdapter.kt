package com.manhua.oh.adapter

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import android.widget.TextView
import com.manhua.oh.R
import com.manhua.oh.activity.ComicActivity

class ChapterSimpleAdapter(
        context: Context,
        data: MutableList<out MutableMap<String, String>>,
        resource: Int,
        from: Array<out String>?,
        to: IntArray?
) : SimpleAdapter(context, data, resource, from, to) {
    private val context = context
    private val data = data

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = super.getView(position, convertView, parent)
        val hm = data[position]
        view.setOnClickListener {
            context.startActivity(Intent(context, ComicActivity::class.java).putExtra("href", hm["href"]))
        }

        val tvName = view.findViewById<TextView>(R.id.tvName)
        if (hm["read"] != null && hm["read"] == true.toString()) {
            tvName.setBackgroundResource(R.drawable.shape_tag)
            tvName.setTextColor(context.resources.getColor(R.color.white))
        } else {
            tvName.setBackgroundResource(R.drawable.shape_chapter)
            tvName.setTextColor(context.resources.getColor(R.color.colorAccent))
        }

        return view as View
    }
}