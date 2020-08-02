package com.manhua.oh.adapter

import android.content.Context
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.manhua.oh.R
import com.manhua.oh.bean.Group

class GroupSimpleAdapter(
    private val context: Context,
    private val data: MutableList<out MutableMap<String, Group>>,
        resource: Int,
        from: Array<out String>?,
        to: IntArray?
) : SimpleAdapter(context, data, resource, from, to) {
    private val sparseArray = SparseArray<ComicAdapter>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if(view == null){
            view = super.getView(position, convertView, parent)
        }

        if(view != null){
            // 刷新界面
            val group = data[position]["group"]

            val tvName = view.findViewById<TextView>(R.id.tvName)
            tvName.text = group?.name

            val tvMore = view.findViewById<TextView>(R.id.tvMore)
            tvMore.text = group?.more

            val rvComic = view.findViewById<RecyclerView>(R.id.rvComic)
            rvComic.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            var comicAdapter = sparseArray[position]
            if(comicAdapter == null){
                comicAdapter = ComicAdapter(context, group!!.comics)
                sparseArray.put(position, comicAdapter)
            }
            rvComic.adapter = comicAdapter
        }

        return view!!

    }
}