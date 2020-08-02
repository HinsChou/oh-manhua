package com.manhua.oh.tool

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.os.Environment
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.manhua.oh.Constant
import com.manhua.oh.R
import java.io.File
import java.io.FileOutputStream

object ComicLoader {
    private val TAG = "ComicLoader"

    var refer = ""

    private fun loadSrc(context: Context, src: String, imageView: ImageView?, listener: Response.Listener<Bitmap>) {
        var path = Environment.getExternalStorageDirectory().absolutePath + File.separator + Constant.DIR
        if (src.contains("/comic/")) {
            path += src.substring(src.indexOf("/comic/"))
        } else if (src.contains("/user/")) {
            path += src.substring(src.indexOf("/user/")) + ".jpg"
        }

        val file = File(path)
//        Log.i(TAG, "path = $path")

        val isCover = path.endsWith("/cover.jpg")
        if (hashMap.containsKey(path)) {
            // 从缓存获取
//            Log.i(TAG, "bitmap from hashMap")
            val bitmap = hashMap[path]
            imageView?.setImageBitmap(bitmap)
            listener.onResponse(bitmap)
        } else if (file.exists() && file.isFile && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // 从文件获取
//            Log.i(TAG, "bitmap from file")
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            imageView?.setImageBitmap(bitmap)
            if(isCover) hashMap[path] = bitmap
            listener.onResponse(bitmap)
        } else {
            if (imageView != null) {
                val animationDrawable: AnimationDrawable = context.getDrawable(R.drawable.animation_loading) as AnimationDrawable
                imageView.setImageDrawable(animationDrawable)
                animationDrawable.start()
            }
            // 从网络获取
            val imageRequest = ImageRequest(src, Response.Listener {
//                    Log.i(TAG, "bitmap from request")
                (context as Activity).runOnUiThread { imageView?.setImageBitmap(it) }
                saveBitmap(context, it, file)
                if(isCover) hashMap[path] = it
                listener.onResponse(it)
            },
                    0,
                    0,
                    ImageView.ScaleType.CENTER_CROP,
                    Bitmap.Config.ARGB_8888,
                    Response.ErrorListener {
                        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.loading_0)
                        hashMap[path] = bitmap
                        if(imageView != null)
                            imageView.setImageResource(R.drawable.loading_0)
                        it.printStackTrace()
                        listener.onResponse(null)
                    }
            )
            VolleyQueue.addRequest(imageRequest)
        }
    }

    fun loadSrc(context: Context, src: String, imageView: ImageView) {
        this.loadSrc(context, src, imageView, Response.Listener { })
    }

    fun loadSrc(context: Context, src: String, listener: Response.Listener<Bitmap>) {
        this.loadSrc(context, src, null, listener)
    }

    private fun saveBitmap(context: Context, bitmap: Bitmap, file: File) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            return

        if (!file.parentFile.exists())
            file.parentFile.mkdirs()
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
    }

    private val hashMap = HashMap<String, Bitmap>()

}