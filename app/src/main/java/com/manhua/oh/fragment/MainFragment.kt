package com.manhua.oh.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.manhua.oh.Constant
import com.manhua.oh.OhDatabase
import com.manhua.oh.R
import com.manhua.oh.adapter.GroupSimpleAdapter
import com.manhua.oh.bean.Comic
import com.manhua.oh.bean.Group
import com.manhua.oh.request.CookieRequest
import com.manhua.oh.tool.ComicLoader
import com.manhua.oh.tool.VolleyQueue
import kotlinx.android.synthetic.main.fragment_main.view.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.concurrent.thread

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var root: View

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        root = inflater.inflate(R.layout.fragment_main, container, false)

        initView()
        initData()

        return root
    }

    private lateinit var groupSimpleAdapter: GroupSimpleAdapter
    private val arrayList = ArrayList<HashMap<String, Group>>()
    private fun initView() {
        groupSimpleAdapter = GroupSimpleAdapter(
                activity as Context, arrayList, R.layout.item_listview_group,
                arrayOf("tvName"), intArrayOf(R.id.tvName)
        )

        root.lvGroup.adapter = groupSimpleAdapter
    }

    private fun initData() {
        val url = Constant.URL + "?t=" + System.currentTimeMillis()
        ComicLoader.refer = url

        val stringRequest = CookieRequest(url, Response.Listener {
            activity?.runOnUiThread(Runnable { root.pbRequest.visibility = View.GONE })
            handleHtml(it)
        }, Response.ErrorListener {
            Log.e(TAG, it.message)
        }, OhDatabase.db.getLogin().cookie
        )

        root.pbRequest.visibility = View.VISIBLE
        thread(start = true) {
            VolleyQueue.addRequest(stringRequest)
        }
    }

    private fun handleHtml(html: String) {
        val groupList = getGroups(Jsoup.parse(html))

        arrayList.clear()
        for (group in groupList) {
            val hashMap = HashMap<String, Group>()
            hashMap.put("group", group)
            arrayList.add(hashMap)
        }

        (context as Activity).runOnUiThread {
            groupSimpleAdapter.notifyDataSetChanged()
        }

    }

    private fun getGroups(document: Document): ArrayList<Group> {
        val groupList = ArrayList<Group>()

        val homes = document.select("div.fed-list-home")
        for (home in homes) {
            val group = Group()

            val xvi = home.select("h2.fed-font-xvi").text()
            Log.i(TAG, "xvi = $xvi")
            group.name = xvi

            val more = home.select("a.fed-more")
            val text = more.text()
            val href = more.attr("href")
            Log.i(TAG, "more = $text[$href]")
            group.more = text
            group.href = href

            val comicList = ArrayList<Comic>()
            val items = home.select("div.fed-part-rows > ul.fed-list-info > li.fed-list-item")
            for (item in items) {
                val comic = Comic()

                val href = item.select("a.fed-list-pics").attr("href")
                comic.href = Constant.URL + href
                comic.dataId = href.replace("/", "")

                val src = item.select("a.fed-list-pics").attr("data-original")
                comic.src = src

                val title = item.select("a.fed-list-title").text()
                comic.title = title

                val desc = item.select("span.fed-list-desc").text()
                comic.lastDate = desc

                val remark = item.select("span.fed-list-remarks").text()
                comic.lastChapter = remark

                comicList.add(comic)
            }

            group.comics = comicList
            groupList.add(group)
        }

        return groupList
    }

}
