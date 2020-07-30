package com.manhua.oh.request

import android.graphics.Bitmap
import android.widget.ImageView
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import java.util.*

class VerifyRequest(
        url: String?,
        listener: Response.Listener<Bitmap>?,
        maxWidth: Int,
        maxHeight: Int,
        scaleType: ImageView.ScaleType?,
        decodeConfig: Bitmap.Config?,
        errorListener: Response.ErrorListener?,
        private val listenerVerify: Response.Listener<String>?
) : ImageRequest(url, listener, maxWidth, maxHeight, scaleType, decodeConfig, errorListener) {

    override fun parseNetworkResponse(response: NetworkResponse?): Response<Bitmap> {
        var verifyId = ""
        for (key in response!!.headers.keys) {
            if (key.toLowerCase(Locale.getDefault()) == "set-cookie") {
                val value = response.headers[key]
                val cookies = value!!.split(";")
                for (cookie in cookies) {
                    val kv = cookie.split("=")
                    if (kv.size == 2 && kv[0] == "KAPTCHA_ID") {
                        verifyId = kv[1]
                    }
                }
            }
        }

        this.listenerVerify!!.onResponse(verifyId)

        return super.parseNetworkResponse(response)
    }

    override fun getBody(): ByteArray {
        return super.getBody()
    }
}