package com.manhua.oh.request

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.manhua.oh.bean.User
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.*

class LoginRequest(method: Int, url: String?, private val params: HashMap<String, String>, private val headers: HashMap<String?, String?>,
                   val listener: Response.Listener<String>, errorListener: Response.ErrorListener?, val headerListener: Response.Listener<User>) :
        Request<String>(method, url, errorListener) {

    private val BOUNDARY =
            "------" + UUID.randomUUID().toString() // 随机生成边界值

    private val NEW_LINE = "\r\n" // 换行符

    private val MULTIPART_FORM_DATA = "multipart/form-data" // 数据类型

    private val charSet = "utf-8" // 编码


    override fun parseNetworkResponse(response: NetworkResponse): Response<String>? {
        return try {
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
            Response.success(
                    String(
                            response.data,
                            Charset.forName(HttpHeaderParser.parseCharset(response.headers))
                    ), HttpHeaderParser.parseCacheHeaders(response)
            )
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            // 解析异常
            Response.error(ParseError(e))
        }
    }

    override fun deliverResponse(response: String?) {
        listener.onResponse(response)
    }

    /**
     * 获取实体的方法，把参数拼接成表单提交的数据格式
     *
     * @return
     * @throws AuthFailureError
     */
    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray? {
        if (params == null || params.size <= 0) {
            return super.getBody()
        }
        // ------WebKitFormBoundarykR96Kta4gvMACHfq                 第一行
        // Content-Disposition: form-data; name="login_username"    第二行
        //                                                          第三行
        // abcde                                                    第四行

        // ------WebKitFormBoundarykR96Kta4gvMACHfq--               结束行

        // 开始拼接数据
        val stringBuffer = StringBuffer()
        for (key in params.keys) {
            val value: Any? = params[key]
            stringBuffer.append("--$BOUNDARY").append(NEW_LINE) // 第一行
            stringBuffer.append("Content-Disposition: form-data; name=\"").append(key).append("\"")
                    .append(NEW_LINE) // 第二行
            stringBuffer.append(NEW_LINE) // 第三行
            stringBuffer.append(value).append(NEW_LINE) // 第四行
        }
        // 所有参数拼接完成，拼接结束行
        stringBuffer.append("--$BOUNDARY--").append(NEW_LINE) // 结束行
        return try {
            stringBuffer.toString().toByteArray(charset(charSet))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            // 使用默认的编码方式，Android为utf-8
            stringBuffer.toString().toByteArray()
        }
    }

    /**
     * 该方法的作用：在 http 头部中声明内容类型为表单数据
     *
     * @return
     */
    override fun getBodyContentType(): String? {
        return MULTIPART_FORM_DATA + ";boundary=" + BOUNDARY
    }


    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String?, String?>? {
        return headers
    }
}