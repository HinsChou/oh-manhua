package com.manhua.oh.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import com.android.volley.Response
import com.manhua.oh.Constant
import com.manhua.oh.database.OhDatabase
import com.manhua.oh.R
import com.manhua.oh.activity.MainActivity
import com.manhua.oh.adapter.LikeSimpleAdapter
import com.manhua.oh.bean.Comic
import com.manhua.oh.request.CookieRequest
import com.manhua.oh.tool.VolleyQueue
import kotlinx.android.synthetic.main.fragment_like.view.*
import kotlinx.android.synthetic.main.fragment_user.*
import org.jsoup.Jsoup
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 */
class LikeFragment : BaseFragment() {

    private lateinit var root: View
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_like, container, false)

        initView()
        initData()
        return root
    }

    private val arrayList = ArrayList<HashMap<String, String>>()
    private lateinit var simpleAdapter: SimpleAdapter
    private fun initView() {
        simpleAdapter = LikeSimpleAdapter(activity as Context, arrayList, R.layout.item_gridview_like,
                arrayOf("tvTitle", "tvChapter", "tvDate"), intArrayOf(R.id.tvTitle, R.id.tvChapter, R.id.tvDate))
        root.gvLike.adapter = simpleAdapter

        root.srlLike.setOnRefreshListener { initData() }
    }

     fun initData() {
        user = OhDatabase.db.getLogin()
        val url = Constant.URL + "/dynamic/user/subscription?t=" + System.currentTimeMillis()
        val stringRequest = CookieRequest(url, Response.Listener {
            handleHtml(it)
            root.srlLike.isRefreshing = false
        }, Response.ErrorListener {
            root.srlLike.isRefreshing = false
            it.printStackTrace()
        }, user.cookie)

        if (user.cookie.isNotEmpty())
            VolleyQueue.addRequest(stringRequest)
        else if(root.srlLike.isRefreshing)
            root.srlLike.isRefreshing = false

    }

    private fun handleHtml(html: String) {
        val documented = Jsoup.parse(html)

        val ul = documented.select("form.fed-user-info > ul.fed-user-list")
        val likes = ul.select("li.fed-line-top > div.fed-user-input")
        Log.i(TAG, "likes ${likes.size}")

        arrayList.clear()
        for (like in likes) {
            val hashMap = HashMap<String, String>()
            val spans = like.select("span")

            val a = spans[1].select("a")
            val href = a.attr("href")
            val dataId = href.replace("/", "")
            // 获取本地数据
            val comic = OhDatabase.db.getComic(dataId)
            comic.dataId = dataId

            val dataLongId = spans[0].select("input.fed-form-comp").attr("value")
            comic.dataLongId = dataLongId

            val title = a.text()
            hashMap["tvTitle"] = title
            comic.title = title

            if (!user.likes.contains(dataId))
                user.likes += "$dataId,"

            val src = Constant.URL + "/comic/" + dataId + "/cover.jpg"
            hashMap["src"] = src
            comic.src = src

            hashMap["href"] = Constant.URL + href
            comic.href = Constant.URL + href

            val last = spans[2].text()
            hashMap["tvChapter"] = last
            comic.lastChapter = last

            val date = spans[3].text()
            hashMap["tvDate"] = date
            comic.lastDate = date

            OhDatabase.db.ohDao().insertComic(comic)
            arrayList.add(hashMap)
        }
        OhDatabase.db.ohDao().updateUser(user)
        simpleAdapter.notifyDataSetChanged()

        // 刷新我的喜欢数量
        (activity as MainActivity).userFragment.tvLike.text = arrayList.size.toString()
    }

}
