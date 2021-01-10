package com.manhua.oh.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.SimpleAdapter
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.manhua.oh.Constant
import com.manhua.oh.database.OhDatabase
import com.manhua.oh.R
import com.manhua.oh.adapter.ChapterSimpleAdapter
import com.manhua.oh.adapter.SearchSimpleAdapter
import com.manhua.oh.request.CookieRequest
import com.manhua.oh.tool.VolleyQueue
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.lang.StringBuilder
import java.net.URLEncoder

/**
 * A simple [Fragment] subclass.
 */
class SearchFragment : BaseFragment() {

    private lateinit var root: View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_search, container, false)

        initView()
        initData()
        return root;
    }

    private val arrayListType = ArrayList<HashMap<String, String>>()
    private fun initData() {
        val chapterSimpleAdapter = ChapterSimpleAdapter(activity as Context, arrayListType, R.layout.item_gridview_chapter,
        arrayOf("tvName"), intArrayOf(R.id.tvName))
        root.gvType.adapter = chapterSimpleAdapter
        requestOrder()
    }

    private fun requestOrder(){
        val url = Constant.URL + "/show"
        val stringRequest = StringRequest(url, {
            handleHtml(it)
        }, {
            it.printStackTrace()
        })
        VolleyQueue.addRequest(stringRequest)
    }

    private fun handleHtml(html : String){
        val document = Jsoup.parse(html)

        arrayListType.clear()
        val types = document.select("div.fed-casc-list > dl")[1].select("dd > a")
        for (type in types) {
            val href = type.attr("href")
            if(href.startsWith("/show?")){
                val text = type.text()

                val hashMap = HashMap<String, String>()
                hashMap["tvName"] = text
                hashMap["href"] = href
                hashMap["read"] = false.toString()

                arrayListType.add(hashMap)
            }
        }

        (root.gvType.adapter as SimpleAdapter).notifyDataSetChanged()
    }

    private val arrayList = ArrayList<HashMap<String, String>>()
    private lateinit var searchSimpleAdapter: SearchSimpleAdapter
    private fun initView() {
        root.etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchComic()
            }
            true
        }

        root.ivSearch.setOnClickListener {
            searchComic()
        }

        searchSimpleAdapter = SearchSimpleAdapter(activity as Context, arrayList, R.layout.item_listview_search,
                arrayOf("tvTitle", "tvState", "tvAuthor", "tvDate", "tvChapter"),
                intArrayOf(R.id.tvTitle, R.id.tvState, R.id.tvAuthor, R.id.tvDate, R.id.tvChapter))
        root.lvSearch.adapter = searchSimpleAdapter
    }

    private fun searchComic() {
        root.pbSearch.visibility = View.VISIBLE
        val key = root.etSearch.text.toString()

        val url = Constant.URL + "/search?searchString=" + URLEncoder.encode(key).replace(" ", "%20")
        Log.i(TAG, "request url = $url")
        val cookieRequest = CookieRequest(url, Response.Listener {
            handleSearch(it)
            root.pbSearch.visibility = View.GONE
        }, Response.ErrorListener {
            Log.e(TAG, "request it = ${it.networkResponse.data}")
            root.pbSearch.visibility = View.GONE
        }, OhDatabase.db.getLogin().cookie)

        VolleyQueue.addRequest(cookieRequest)
    }

    private fun handleSearch(html: String) {
        val document = Jsoup.parse(html)

        arrayList.clear()
        val dls = document.select("div.fed-part-layout > dl.fed-deta-info")
        for (dl in dls) {
            arrayList.add(fillComic(dl))
        }
        gvType.visibility = if(dls.isEmpty()) View.VISIBLE else View.GONE

        searchSimpleAdapter.notifyDataSetChanged()
    }

    private fun fillComic(dl: Element): HashMap<String, String> {
        val hashMap = HashMap<String, String>()

        val title = dl.select("dd.fed-deta-content > h1.fed-part-eone > a").text()
        hashMap["tvTitle"] = title

        val src = dl.select(" > dt.fed-deta-images > a.fed-list-pics").attr("data-original")
        hashMap["src"] = src

        val href = dl.select("dd.fed-deta-content > h1.fed-part-eone > a").attr("href")
        hashMap["href"] = Constant.URL + href

        val lis = dl.select("dd.fed-deta-content > ul > li")

        hashMap["tags"] = ""
        for (li in lis) {
            val span = li.select("span.fed-text-muted").text()
            val a = li.select("a")
            val text = li.text().replace(span, "")
            when (span) {
                "状态" -> hashMap["tvState"] = text
                "作者" -> hashMap["tvAuthor"] = text
                "更新" -> hashMap["tvDate"] = text
                "最新" -> hashMap["tvChapter"] = text
                "类别" -> {
                    for (type in a) {
                        hashMap["tags"] += type.text() + ","
                    }
                }
            }
        }

        return hashMap
    }
}
