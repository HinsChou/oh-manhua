package com.manhua.oh.request

import com.android.volley.Response
import com.android.volley.toolbox.StringRequest

class CookieRequest(
        url: String?,
        listener: Response.Listener<String>?,
        errorListener: Response.ErrorListener?,
        val cookie: String
) : StringRequest(url, listener, errorListener) {

    override fun getHeaders(): MutableMap<String, String> {
        val hashMap = HashMap<String, String>()
        hashMap["cookie"] = "login_cookie=$cookie"
        return hashMap
    }
}