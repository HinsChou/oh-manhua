package com.manhua.oh.request

import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.manhua.oh.bean.User
import com.manhua.oh.database.OhDatabase

class CookieRequest(
        url: String?,
        listener: Response.Listener<String>?,
        errorListener: Response.ErrorListener?,
        val cookie: String
) : StringRequest(url, listener, errorListener) {

    override fun getHeaders(): MutableMap<String, String> {
        val user = OhDatabase.db.getLogin()
        val hashMap = HashMap<String, String>()
        hashMap["cookie"] = "login_cookie=" + user.cookie
        hashMap["user-agent"] = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36"
        return hashMap
    }
}