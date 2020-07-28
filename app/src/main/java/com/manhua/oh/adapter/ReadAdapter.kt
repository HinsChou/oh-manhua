package com.manhua.oh.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.manhua.oh.R
import com.manhua.oh.fragment.MenuFragment


class ReadAdapter(
    private val context: Context,
    private val comicList: ArrayList<Bitmap>,
    val layoutId: Int
) :
        RecyclerView.Adapter<ReadAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.ivRead);
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        view.setOnClickListener {
            val menuFragment = MenuFragment()
            menuFragment.show((context as AppCompatActivity).supportFragmentManager, "")
        }
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return comicList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.setImageBitmap(comicList[position])

    }
}