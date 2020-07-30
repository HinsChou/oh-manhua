package com.manhua.oh.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import com.android.volley.Response
import com.manhua.oh.Constant
import com.manhua.oh.OhDatabase
import com.manhua.oh.R
import com.manhua.oh.adapter.LikeSimpleAdapter
import com.manhua.oh.request.CookieRequest
import com.manhua.oh.tool.VolleyQueue
import kotlinx.android.synthetic.main.fragment_like.view.*
import org.jsoup.Jsoup

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
                arrayOf("tvName", "tvLast", "tvDate"), intArrayOf(R.id.tvName, R.id.tvLast, R.id.tvDate))
        root.gvLike.adapter = simpleAdapter

        root.srlLike.setOnRefreshListener { initData() }
    }

    private fun initData() {
        val url = "https://www.ohmanhua.com/dynamic/user/subscription?t=" + System.currentTimeMillis()
        val stringRequest = CookieRequest(url, Response.Listener {
            handleHtml(it)
            root.srlLike.isRefreshing = false
        }, Response.ErrorListener {
            root.srlLike.isRefreshing = false
            it.printStackTrace()
        }, user.cookie)

        if (user.cookie.isNotEmpty())
            VolleyQueue.addRequest(stringRequest)

    }

    private fun handleHtml(html: String) {
        val documented = Jsoup.parse(html)

        val ul = documented.select("form.fed-user-info > ul.fed-user-list")
        val likes = ul.select("li.fed-line-top > div.fed-user-input")

        arrayList.clear()
        for (like in likes) {
            val hashMap = HashMap<String, String>()

            val spans = like.select("span")
            val a = spans[1].select("a")
            val title = a.text()
            hashMap["tvName"] = title

            val href = a.attr("href")
            val dataId = href.replace("/", "")
            if (!user.likes.contains(dataId))
                user.likes += "$dataId,"

            var src = Constant.URL + "/comic/" + dataId + "/cover.jpg"
            hashMap["src"] = src

            hashMap["href"] = Constant.URL + href

            val last = spans[2].text()
            hashMap["tvLast"] = last

            val date = spans[3].text()
            hashMap["tvDate"] = date

            arrayList.add(hashMap)
        }
        OhDatabase.db.userDao().update(user)

        simpleAdapter.notifyDataSetChanged()
    }

}
