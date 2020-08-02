package com.manhua.oh.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SimpleAdapter
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.manhua.oh.Constant
import com.manhua.oh.database.OhDatabase
import com.manhua.oh.R
import com.manhua.oh.adapter.ChapterSimpleAdapter
import com.manhua.oh.bean.Comic
import com.manhua.oh.bean.Record
import com.manhua.oh.request.FormRequest
import com.manhua.oh.tool.ComicLoader
import com.manhua.oh.tool.Snack
import com.manhua.oh.tool.VolleyQueue
import kotlinx.android.synthetic.main.activity_cover.*
import kotlinx.android.synthetic.main.fragment_like.*
import org.jsoup.Jsoup
import org.jsoup.select.Elements

class CoverActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cover)

        initView()
        initData()
    }

    override fun onResume() {
        super.onResume()
        updateRecord()
    }

    private var href = ""
    private fun initView() {
        href = intent.getStringExtra("href")
        val title = intent.getStringExtra("title")

        val dataId = href.replace(Constant.URL, "").replace("/", "")
        comic = OhDatabase.db.getComic(dataId)
        comic.dataId = dataId
//        ComicLoader.loadSrc(getActivity(), comic.src, ivCover)
        tvTitle.text = title
//        tvAuthor.text = comic.author
//        tvDate.text = comic.lastDate
//        tvChapter.text = comic.lastChapter

        ivBack.setOnClickListener { finish() }
        fabLike.setOnClickListener {
            if (user.likes.contains("${comic.dataId},"))
                hateComic()
            else
                likeComic()
        }

        onTabSelected.setTabs(arrayOf(tvBrief, gvTag))
        tlOther.addOnTabSelectedListener(onTabSelected)
    }

    private val onTabSelected = object : TabLayout.OnTabSelectedListener {
        private lateinit var tabs : Array<View>
        fun setTabs(tabs : Array<View>){
            this.tabs = tabs
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            tabs[tab!!.position].visibility = View.GONE
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {
            tabs[tab!!.position].visibility = View.VISIBLE
        }

    }

    private fun likeComic() {
        val url = "https://www.ohmanhua.com/dynamic/user/addToFav"
        val params = HashMap<String, String>()
        params["dataId"] = comic.dataId

        val headers = HashMap<String, String>()
        headers["cookie"] = "login_cookie=" + user.cookie

        val formRequest = FormRequest(Request.Method.POST, url, params, headers, Response.Listener {
            if (it.status == "S") {
                fabLike.setImageResource(R.mipmap.icon_like_solid)
                if (!user.likes.contains(comic.dataId)) {
                    user.likes += "${comic.dataId},"
                    OhDatabase.db.ohDao().updateUser(user)
                }
            } else {
                Snackbar.make(gvLike, it.message, Snackbar.LENGTH_LONG).show()
            }
        },
                Response.ErrorListener {
                    it.printStackTrace()
                })
        VolleyQueue.addRequest(formRequest)
    }

    private fun hateComic() {
        val url = "https://www.ohmanhua.com/dynamic/user/subscriptionHandle"
        val params = HashMap<String, String>()
        params["dataIds"] = comic.dataLongId
        params["type"] = "1"

        val headers = HashMap<String, String>()
        headers["cookie"] = "login_cookie=" + OhDatabase.db.getLogin().cookie

        val formRequest = FormRequest(Request.Method.POST, url, params, headers, Response.Listener {
            if (it.status == "S") {
                fabLike.setImageResource(R.mipmap.icon_like_stroke)

                if (!user.likes.contains(comic.dataId)) {
                    user.likes = user.likes.replace("${comic.dataId},", "")
                    OhDatabase.db.ohDao().updateUser(user)
                }
            } else {
                Snack.show(getActivity(), it.message)
            }
        },
                Response.ErrorListener {
                    it.printStackTrace()
                })
        VolleyQueue.addRequest(formRequest)
    }

    private lateinit var arrayAdapter: SimpleAdapter
    private val tagList: ArrayList<HashMap<String, String>> = ArrayList()
    private var record = Record()
    private fun initData() {
        arrayAdapter = ChapterSimpleAdapter(getActivity(), tagList, R.layout.item_gridview_chapter,
                arrayOf("tvName"), intArrayOf(R.id.tvName))
        gvTag.adapter = arrayAdapter

        // 上次阅读
        fabRead.setOnClickListener {
            record = OhDatabase.db.getRecordComic(comic.dataId)
            val intent = Intent(getActivity(), ComicActivity::class.java)
            intent.putExtra("href", href + "1/${record.chapterId}.html")
            startActivity(intent)
        }

        // 刷新收藏
        user = OhDatabase.db.getLogin()
        if (user.likes.contains("${comic.dataId},")) {
            if(comic.dataLongId.isEmpty()){
                fabLike.visibility = View.INVISIBLE
            }else{
                fabLike.setImageResource(R.mipmap.icon_like_solid)
            }
        }

        // 加载界面
        loadComic(href)
    }

    private fun loadComic(href: String) {
        Log.i(TAG, "loadCover = $href")

        val stringRequest = StringRequest(href, Response.Listener {
            handleDetail(it)
        }, Response.ErrorListener {
            it.printStackTrace()
        })
        VolleyQueue.addRequest(stringRequest)
    }

    private var comic = Comic()
    private fun handleDetail(html: String) {
        val document = Jsoup.parse(html)

        val dl = document.select("dl.fed-deta-info")
        fillComic(comic, dl)

        val tabs = document.select("div.fed-tabs-info > div.fed-tabs-boxs > div.fed-tabs-item")

        val brief = tabs[1].select("p").text()
        tvBrief.text = brief
        comic.brief = brief

        val chapters = tabs[0].select("div.fed-visible > div.all_data_list > ul > li > a")
        Log.i(TAG, "chapters = ${chapters.size}")
        tagList.clear()
        for (chapter in chapters) {
            val hashMap = HashMap<String, String>()
            hashMap["tvName"] = chapter.text()
            val href = Constant.URL + chapter.attr("href")
            if (href.contains("/${record.chapterId}.html")) {
                hashMap["read"] = true.toString()
            }
            hashMap["href"] = href
            tagList.add(hashMap)
        }
        arrayAdapter.notifyDataSetChanged()

    }

    private fun fillComic(comic: Comic, dl: Elements) {
        val src = dl.select(" > dt.fed-deta-images > a.fed-list-pics").attr("data-original")
        ComicLoader.loadSrc(getActivity(), src, ivCover)

        val lis = dl.select("dd.fed-deta-content > ul > li")

        for (li in lis) {
            val span = li.select("span.fed-text-muted").text()
            val a = li.select("a")
            when (span) {
                "状态" -> comic.type = a.text()
                "作者" -> comic.author = a.text()
                "更新" -> comic.lastDate = a.text()
                "最新" -> comic.lastChapter = a.text()
                "类别" -> {
                    var count = 0
                    for (type in a) {
                        if (count == 0) {
                            tvType1.text = type.text()
                            tvType1.visibility = View.VISIBLE
                        }
                        if (count == 1) {
                            tvType2.text = type.text()
                            tvType2.visibility = View.VISIBLE
                        }
                        if (count == 2) {
                            tvType3.text = type.text()
                            tvType3.visibility = View.VISIBLE
                        }
                        count++
                        comic.tags += type.text() + ","
                    }
                }
            }
        }

        tvState.text = comic.type
        tvAuthor.text = comic.author
        tvDate.text = comic.lastDate
        tvChapter.text = comic.lastChapter
    }

    private fun updateRecord() {
        record = OhDatabase.db.getRecordComic(comic.dataId)
        for (hashMap in tagList) {
            if (hashMap["href"]!!.contains("/${record.chapterId}.html")) {
                hashMap["read"] = true.toString()
            } else {
                hashMap["read"] = false.toString()
            }
        }
        arrayAdapter.notifyDataSetChanged()
    }

}
