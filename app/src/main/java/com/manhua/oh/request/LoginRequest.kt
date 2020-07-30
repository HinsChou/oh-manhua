package com.manhua.oh.request

import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.manhua.oh.bean.Result
import com.manhua.oh.bean.User
import java.net.URLDecoder
import java.util.*

class LoginRequest(
    method: Int,
    url: String?,
    params: HashMap<String, String>,
    headers: HashMap<String, String>,
    listener: Response.Listener<Result>,
    errorListener: Response.ErrorListener?,
    val headerListener: Response.Listener<User>
) : FormRequest(method, url, params, headers, listener, errorListener) {

    override fun parseNetworkResponse(response: NetworkResponse): Response<Result>? {
        val user = User()
        for (header in response.allHeaders) {
            if (header.name.toLowerCase(Locale.getDefault()) == "set-cookie") {
                val values = header.value.split(";")
                for (value in values) {
                    if (value.contains("user_id")) {
                        user.userId = value.split("user_id=")[1].split(";")[0]
                    }
                    if (value.contains("login_cookie")) {
                        user.cookie = value.split("login_cookie=")[1].split(";")[0]
                    }
                    if (value.contains("display_name")) {
                        user.nickName = value.split("display_name=")[1].split(";")[0]
                        user.nickName = URLDecoder.decode(user.nickName)
                    }
                }
            }
        }
        headerListener.onResponse(user)

        return super.parseNetworkResponse(response)
    }

}