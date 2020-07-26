package com.manhua.oh.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.manhua.oh.R
import com.manhua.oh.bean.Group

class GroupSimpleAdapter(
        context: Context,
        data: MutableList<out MutableMap<String, Group>>,
        resource: Int,
        from: Array<out String>?,
        to: IntArray?
) : SimpleAdapter(context, data, resource, from, to) {
    private val context = context
    private val data = data

    private val hashMap = HashMap<Int, View>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        var view = hashMap[position]
//        if(view == null){
        val view = super.getView(position, convertView, parent)
//            hashMap[position] = view

        // 刷新界面
        val group = data[position]["group"]

        val tvName = view.findViewById<TextView>(R.id.tvName)
        tvName.text = group?.name

        val tvMore = view.findViewById<TextView>(R.id.tvMore)
        tvMore.text = group?.more

        val rvComic = view.findViewById<RecyclerView>(R.id.rvComic)
        rvComic.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val comicAdapter = ComicAdapter(context, group!!.comics)
        rvComic.adapter = comicAdapter
//        }

        return view as View
    }
}