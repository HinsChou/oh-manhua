package com.manhua.oh.fragment

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.google.android.material.snackbar.Snackbar
import com.manhua.oh.Constant
import com.manhua.oh.OhDatabase
import com.manhua.oh.R
import com.manhua.oh.adapter.ComicAdapter
import com.manhua.oh.bean.Comic
import com.manhua.oh.request.CookieRequest
import com.manhua.oh.request.LoginRequest
import com.manhua.oh.request.VerifyRequest
import com.manhua.oh.tool.ComicLoader
import com.manhua.oh.tool.VolleyQueue
import kotlinx.android.synthetic.main.fragment_user.view.*
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 */
class UserFragment : BaseFragment() {
    private lateinit var root: View

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_user, container, false)

        initView()
        initData()

        return root
    }

    private val records = ArrayList<Comic>()
    private fun initView() {
        root.rvRecord.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val comicAdapter = ComicAdapter(activity as Context, records)
        root.rvRecord.adapter = comicAdapter
    }

    private fun initData() {
        val user = OhDatabase.db.getLogin()
        val cookie = user.cookie
        Log.i(TAG, "cookie = $cookie")
        if (cookie.isEmpty()) {
            root.clSignIn.visibility = View.VISIBLE
            root.clRecord.visibility = View.INVISIBLE
            updateVerify()
            root.fabLogin.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
            root.fabLogin.setOnClickListener { login() }
            root.fabLogin.setImageResource(R.mipmap.icon_enter)

        } else {
            root.clSignIn.visibility = View.INVISIBLE
            root.clRecord.visibility = View.VISIBLE

            val src = Constant.URL + "/user/image/" + user.userId
            ComicLoader.loadSrc(activity as Context, src, root.ivHead)
            root.tvName.text = user.nickName
            root.fabLogin.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.red))
            root.fabLogin.setOnClickListener { logOut() }
            root.fabLogin.setImageResource(R.mipmap.icon_error)

            // 请求历史记录
            val url = "https://www.ohmanhua.com/dynamic/user/history"
            val cookieRequest = CookieRequest(url, Response.Listener {
                handleHtml(it)
            }, Response.ErrorListener {
                it.printStackTrace()
            }, user.cookie)
            VolleyQueue.addRequest(cookieRequest)
        }
    }

    private fun handleHtml(html: String) {
        val document = Jsoup.parse(html)

        val lines = document.select("ul.fed-user-list > li.fed-line-top > div.fed-user-input")
        for (line in lines) {
            val comic = Comic()
            val spans = line.select("span.fed-part-eone")
            val title = spans[1].text()
            comic.title = title

            val href = spans[1].select("a").attr("href")
            comic.dataId = href.replace("/", "")
            comic.src = Constant.URL + "/comic" + href + "cover.jpg"
            comic.href = Constant.URL + href

            val readTime = spans[2].text()
            comic.readTime = readTime

            val readChapter = spans[3].text()
            comic.readChapter = readChapter

            val readHref = spans[3].select("a").attr("href")
            comic.readHref = readHref

            val lastChapter = spans[4].text()
            comic.lastChapter = lastChapter

            val lastHref = spans[4].select("a").attr("href")
            comic.lastHref = lastHref

            records.add(comic)

            // 更新本地历史
            val record = OhDatabase.db.getRecordComic(comic.dataId)
            record.chapterId =
                comic.readHref.replace("/${comic.dataId}/1/", "").replace(".html", "")
            record.timestamp = simpleDateFormat.parse(comic.readTime).time
            if (record.dataId.isEmpty()) {
                record.dataId = comic.dataId
                record.userId = user.userId
                OhDatabase.db.recordDao().insert(record)
            } else {
                OhDatabase.db.recordDao().update(record)
            }
        }

        root.rvRecord.adapter!!.notifyDataSetChanged()
    }

    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    private var verifyId = ""
    private fun updateVerify() {
        val url = "https://www.ohmanhua.com/dynamic/kaptcha?v=" + System.currentTimeMillis()
        val imageRequest = VerifyRequest(url, Response.Listener {
            resources.displayMetrics.scaledDensity
            Log.i(TAG, "smallDp = ")
            root.ivVerify.setImageBitmap(it)
        }, 0, 0, ImageView.ScaleType.FIT_XY, Bitmap.Config.ARGB_8888, Response.ErrorListener() {
            Log.e(TAG, it.message)
        }, Response.Listener {
            verifyId = it
        })

        VolleyQueue.addRequest(imageRequest)
    }

    private fun login() {
        val url = "https://www.ohmanhua.com/user/loginHandle?v=" + System.currentTimeMillis()

        val params = HashMap<String, String>()
        params.put("user_name", root.etUsername.text.toString())
        params.put("user_pwd", root.etPassword.text.toString())
        params.put("verifyCode", root.etVerify.text.toString())
        params.put("remeberModule", "30")

        val header = HashMap<String, String>()
        header.put("cookie", "KAPTCHA_ID=$verifyId")

        val formRequest =
            LoginRequest(Request.Method.POST, url, params, header, Response.Listener {
                    Log.i(TAG, "listener $it")
                if (it.status == "S") {
                        initData()
                    } else {
                    updateVerify()
                    Snackbar.make(root.fabLogin, it.message, Snackbar.LENGTH_LONG).show()
                    }
                }, Response.ErrorListener {
                    Log.e(TAG, "errorListener ${it.message}")
                }, Response.Listener {
                    Log.i(TAG, "cookies " + it.toString())
                    it.username = params["user_name"]!!
                    it.password = params["user_pwd"]!!
                    OhDatabase.db.userDao().insert(it)
                })

        VolleyQueue.addRequest(formRequest)
    }

    private fun logOut() {
        val user = OhDatabase.db.getLogin()
        user.cookie = ""
        OhDatabase.db.userDao().update(user)

        initData()
    }
}
