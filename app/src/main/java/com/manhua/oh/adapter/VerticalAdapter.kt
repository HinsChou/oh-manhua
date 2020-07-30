package com.manhua.oh.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.manhua.oh.BaseApplication
import com.manhua.oh.R
import com.manhua.oh.fragment.MenuFragment


class VerticalAdapter(
    private val context: Context,
    private val comicList: ArrayList<Bitmap>,
    val layoutId: Int
) :
    RecyclerView.Adapter<VerticalAdapter.ViewHolder>() {

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
        val imageView = holder.imageView
        val bitmap = comicList[position]
        if (bitmap.width < BaseApplication.widthPixels) {
            val scale = BaseApplication.widthPixels * 1f / bitmap.width
            val matrix = Matrix()
            matrix.postScale(scale, scale)
            val bitmap0 =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            imageView.setImageBitmap(bitmap0)
        } else {
            imageView.setImageBitmap(bitmap)
        }
    }
}