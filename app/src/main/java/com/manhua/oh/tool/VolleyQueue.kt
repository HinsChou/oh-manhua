package com.manhua.oh.tool

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.Volley
import com.manhua.oh.request.FormRequest

object VolleyQueue {
    private lateinit var requestQueue: RequestQueue

    public fun init(context: Context) {
        requestQueue = Volley.newRequestQueue(context)
    }

    public fun addRequest(imageRequest: ImageRequest) {
        requestQueue.add(imageRequest)
    }

    public fun addRequest(stringRequest: Request<String>) {
        requestQueue.add(stringRequest)
    }

    public fun addRequest(stringRequest: FormRequest) {
        requestQueue.add(stringRequest)
    }
}