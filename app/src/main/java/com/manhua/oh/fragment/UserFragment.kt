package com.manhua.oh.fragment

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.manhua.oh.Constant
import com.manhua.oh.database.OhDatabase
import com.manhua.oh.R
import com.manhua.oh.adapter.ComicAdapter
import com.manhua.oh.bean.Comic
import com.manhua.oh.request.CookieRequest
import com.manhua.oh.tool.ComicLoader
import com.manhua.oh.tool.VolleyQueue
import kotlinx.android.synthetic.main.fragment_user.view.*
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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

    fun initData() {
        val user = OhDatabase.db.getLogin()
        val cookie = user.cookie
        Log.i(TAG, "cookie = $cookie")
        if (cookie.isEmpty()) {
            root.clRecord.visibility = View.INVISIBLE

            root.tvNickName.text = getString(R.string.not_login)
            root.tvUserId.text = user.userId
            root.tvUserId.visibility = View.INVISIBLE
            root.tvUserName.text = user.username

            root.fabLogin.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
            root.fabLogin.setOnClickListener { showLogin() }
            root.fabLogin.setImageResource(R.mipmap.icon_enter)

        } else {
            root.clRecord.visibility = View.VISIBLE

            val src = Constant.URL + "/user/image/" + user.userId
            ComicLoader.loadSrc(activity as Context, src, root.ivHead)
            root.tvNickName.text = user.nickName
            root.tvUserId.text = user.userId
            root.tvUserName.text = user.username

            root.fabLogin.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.red))
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

    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
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

            val readChapter = spans[3].text()

            val readHref = spans[3].select("a").attr("href")

            val lastChapter = spans[4].text()
            comic.lastChapter = lastChapter

            val lastHref = spans[4].select("a").attr("href")
            comic.lastHref = lastHref

            records.add(comic)

            // 更新本地历史
            val record = OhDatabase.db.getRecordComic(comic.dataId)
            record.chapterId = readHref.replace("/${comic.dataId}/1/", "").replace(".html", "")
            record.timestamp = simpleDateFormat.parse(readTime).time
            if (record.dataId.isEmpty()) {
                record.dataId = comic.dataId
                record.userId = user.userId
                OhDatabase.db.ohDao().insertRecord(record)
            } else {
                OhDatabase.db.ohDao().updateRecord(record)
            }
        }

        root.rvRecord.adapter!!.notifyDataSetChanged()

        root.tvRead.text = records.size.toString()
    }

    private fun showLogin(){
        val loginFragment = LoginFragment()
        loginFragment.show(activity!!.supportFragmentManager, "")
    }

    private fun logOut() {
        val user = OhDatabase.db.getLogin()
        user.cookie = ""
        OhDatabase.db.ohDao().updateUser(user)

        initData()
    }
}
