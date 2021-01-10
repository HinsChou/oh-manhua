package com.manhua.oh.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64.DEFAULT
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.Request
import com.android.volley.Response
import com.manhua.oh.Constant
import com.manhua.oh.database.OhDatabase
import com.manhua.oh.R
import com.manhua.oh.adapter.ReadAdapter
import com.manhua.oh.adapter.VerticalAdapter
import com.manhua.oh.bean.Chapter
import com.manhua.oh.bean.Record
import com.manhua.oh.request.CookieRequest
import com.manhua.oh.request.FormRequest
import com.manhua.oh.tool.ComicLoader
import com.manhua.oh.tool.Snack
import com.manhua.oh.tool.VolleyQueue
import kotlinx.android.synthetic.main.activity_comic.*
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.nio.charset.Charset
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class ComicActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comic)

        initView()
        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        vpComic.unregisterOnPageChangeCallback(onPageChangeCb)
        rvComic.removeOnScrollListener(onScrollL)
        countRequest = 1
    }

    public fun loadPrev() {
        if (chapter.prev.isEmpty()) {
            Snack.show(getActivity(), getString(R.string.first))
        } else {
            href = chapter.prev
            reload()
        }
    }

    public fun loadNext() {
        if (chapter.next.isEmpty()) {
            Snack.show(getActivity(), getString(R.string.last))
        } else {
            href = chapter.next
            reload()
        }
    }

    public fun toggleDirect(): Int {
        return if (vpComic.visibility == View.VISIBLE) {
            rvComic.visibility = View.VISIBLE
            vpComic.visibility = View.GONE

            RecyclerView.VERTICAL
        } else {
            rvComic.visibility = View.GONE
            vpComic.visibility = View.VISIBLE
            RecyclerView.HORIZONTAL
        }
    }

    private val bitmaps = ArrayList<Bitmap>()
    private fun initView() {
        ivBack.setOnClickListener { finish() }
        sStart.setOnClickListener {
            if (vpComic.currentItem == 0) {
                loadPrev()
            } else {
                vpComic.currentItem --
                updateVisible()
            }
        }
        sEnd.setOnClickListener {
            if (chapter.page != 0 && vpComic.currentItem == chapter.page - 1) {
                loadNext()
            } else {
                vpComic.currentItem ++
                updateVisible()
            }
        }

        val vpAdapter = ReadAdapter(getActivity(), bitmaps, R.layout.item_viewpager_comic)
        vpComic.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        vpComic.adapter = vpAdapter
        onPageChangeCb = OnPageChangeCb(this)
        vpComic.registerOnPageChangeCallback(onPageChangeCb)

        val rvAdapter = VerticalAdapter(getActivity(), bitmaps, R.layout.item_listview_comic)
        rvComic.layoutManager =
            LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false)
        rvComic.adapter = rvAdapter
        onScrollL = OnScrollL(this)
        rvComic.addOnScrollListener(onScrollL)
    }

    private fun reload() {
        chapter.page = 0
        loadedPage = -1
        bitmaps.clear()
        updateAdapter()
        updatePage()
        initData()
    }

    private fun updateAdapter() {
        rvComic.adapter?.notifyDataSetChanged()
        vpComic.adapter?.notifyDataSetChanged()
    }

    private lateinit var onPageChangeCb: OnPageChangeCb
    private lateinit var onScrollL: OnScrollL

    private class OnPageChangeCb(val comicActivity: ComicActivity) :
        ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            Log.i(comicActivity.TAG, "onPageSelected $position")
            comicActivity.updateCurrent(position)
            if(comicActivity.rvComic.visibility != View.VISIBLE)
                comicActivity.rvComic.scrollToPosition(position)
        }
    }

    private class OnScrollL(val comicActivity: ComicActivity) : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val position = (comicActivity.rvComic.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                Log.i(comicActivity.TAG, "onScrollStateChanged $position")
                comicActivity.updateCurrent(position)
                if(comicActivity.vpComic.visibility != View.VISIBLE)
                    comicActivity.vpComic.setCurrentItem(position, false)
            }
        }
    }

    var current = 0
    fun updateCurrent(position: Int) {
        Log.i(TAG, "updateCurrent $position")
        current = position
        updatePage()
        if(current == loadedPage)
            pbRequest.visibility = View.VISIBLE
        addComic(loadedPage + 1)
    }

    fun updateVisible(){
        if(vpComic.visibility == View.VISIBLE)
            vpComic.currentItem = current
        if(rvComic.visibility == View.VISIBLE)
            rvComic.smoothScrollToPosition(current)
    }

    private fun updateRecord(position: Int) {
        record.page = position
        record.timestamp = System.currentTimeMillis()
        OhDatabase.db.ohDao().insertRecord(record)
    }

    private var href = ""
    private var record = Record()
    private fun initData() {
        if (href.isEmpty())
            href = intent.getStringExtra("href")
        Log.i(TAG, "href = ${href}")

        chapter = Chapter()
        var ids = href.replace(Constant.URL, "").split("/")
        val dataId = ids[1]
        val chapterId = ids[3].replace(".html", "")

        record = OhDatabase.db.getRecordChapter(dataId, chapterId)
        record.chapterId = chapterId
        record.dataId = dataId
        record.userId = user.userId
        updateRecord(0)

        loadComic(href)
    }

    private fun loadComic(href: String) {
        Log.i(TAG, "loadComic = $href")
        pbRequest.visibility = View.VISIBLE

        val url = href + "?t=" + System.currentTimeMillis()
        val stringRequest = CookieRequest(url, Response.Listener {
            handleDetail(it)
        }, Response.ErrorListener {
            it.printStackTrace()
        }, OhDatabase.db.getLogin().cookie)
        VolleyQueue.addRequest(stringRequest)
    }

    private var chapter = Chapter()
    private fun handleDetail(html: String) {
        val document = Jsoup.parse(html)

        val javascripts = document.select("script")
        // 获取页码
        var dataPage = ""
        var dataPageId = ""
        for (javascript in javascripts) {
            val text = javascript.html()
            if (text.contains("C_DATA")) {
                dataPage = text.split("\'")[1]
            }

            if (text.contains("__jsData")) {
                dataPageId = text.replace("__jsData =", "")
            }
        }

        var key = "fw122587mkertyui"
        var mh = decrypt(key, dataPage)
        if(mh.contains("{"))
            mh = mh.substring(mh.indexOf("{"), mh.indexOf("}") + 1)
        val joMh = JSONObject(mh)
        var total = 0
        Log.i(TAG, "joMh $joMh")
        if(joMh.has("totalimg"))
            total = joMh["totalimg"].toString().toInt()
        else if(joMh.has("enc_code1")){
            total = decrypt(key, joMh.getString("enc_code1")).toInt()
        }

        chapter.page = total
        Log.i(TAG, "total = $total")
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.loading_0)
        for (i in 0 until total) {
            bitmaps.add(bitmap)
        }
        vpComic.currentItem = 0
        updatePage()
        updateAdapter()

        // 获取章节id
        val joPageId = JSONObject(dataPageId)
        val pageId = joPageId["dataPageId"].toString()
        chapter.pageId = pageId
        uploadRecord(pageId)

        val readend = document.select("div.mh_readend")

        val title = readend.select("a").attr("title")
        Log.i(TAG, "title = $title")
        chapter.title = title
        tvTitle.text = title

        val lis = readend.select("ul > li > a")
        val dataId = lis[1].attr("href")

        val prev = lis[0].attr("href")
        Log.i(TAG, "prev = $prev")
        if (!prev.startsWith("javascript:"))
            chapter.prev = Constant.URL + prev
        val next = lis[2].attr("href")
        Log.i(TAG, "next = $next")
        if (!next.startsWith("javascript:"))
            chapter.next = Constant.URL + next

        var prefix = Constant.URL + "/comic" + dataId
        if(joMh.has("enc_code2")){
            var enc_code2 = decrypt("fw125gjdi9ertyui", joMh.getString("enc_code2"))
            prefix += enc_code2.split("/")[1] + "/"
        }else{
            prefix += URLEncoder.encode(title).replace("+", "%20") + "/"
        }

        chapter.prefix = prefix
        Log.i(TAG, "prefix = $prefix")
    }

    private fun uploadRecord(pageId: String) {
        if (user.cookie.isEmpty())
            return

        val url = Constant.URL + "/counting"
        val headers = HashMap<String, String>()
        headers.put("cookie", "login_cookie=" + user.cookie)
        val params = HashMap<String, String>()
        params["pageId"] = pageId

        val formRequest = FormRequest(Request.Method.POST, url, params, headers, Response.Listener {
            Log.i(TAG, "$url ${it.status} ${it.message}")
        }, Response.ErrorListener {
            it.printStackTrace()
        })
        VolleyQueue.addRequest(formRequest)
    }

    private fun updatePage() {
        val current = vpComic.currentItem + 1
        tvPage.text = "$current / "
        tvTotal.text = bitmaps.size.toString()
        if (chapter.page != 0 && bitmaps.size == chapter.page) {
            tvTotal.setTextColor(resources.getColor(R.color.colorAccent))
        } else {
            tvTotal.setTextColor(resources.getColor(R.color.red))
        }
        pbPage.max = bitmaps.size
        pbPage.progress = vpComic.currentItem + 1
        pbPage.secondaryProgress = loadedPage
    }

    private var loadedPage = -1 // 已加载页
    private val maxLoad = 5 // 最大加载数
    private var countRequest = 0 // 请求数
    private val handler = Handler(Looper.getMainLooper())
    private fun addComic(loadPage : Int) {
        Log.i(
            TAG,
            "addComic: currentPage ${vpComic.currentItem},loadPage $loadPage,maxPage ${chapter.page},countRequest $countRequest"
        )

        // 同时请求1个, 加载页不超过结尾页, 与当前页相差不超过最大缓存
        if (countRequest >= 1 || loadPage >= chapter.page || loadPage - vpComic.currentItem >= maxLoad) {
            Log.i(TAG, "addComic return")
            pbRequest.visibility = View.GONE
            return
        }
        countRequest++

        var page = String.format("%04d", loadPage + 1)
        val src = "${chapter.prefix}$page.jpg"
        val position = loadPage
        ComicLoader.loadSrc(getActivity(), src, Response.Listener {
            Log.i(TAG, "loadSrc finish = $src")
            countRequest--
            runOnUiThread {
                pbRequest.visibility = View.GONE
                updatePage()
            }

            if (it != null){
                if (position < bitmaps.size)
                    bitmaps[position] = it
                handler.postDelayed({
                    updateAdapter()
                }, 100)
                loadedPage = loadPage

                handler.postDelayed({
                    addComic(loadedPage + 1)
                }, 1000)
            }
        })
    }

    private fun decrypt(sKey: String, sSrc: String): String {
        return try {
            val raw = sKey.toByteArray(charset("utf-8"))
            val skeySpec =
                SecretKeySpec(raw, "AES")
            val cipher =
                Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, skeySpec)
            // 两次解码
            var data = android.util.Base64.decode(sSrc, DEFAULT)
            data = android.util.Base64.decode(data, DEFAULT)
            try {
                val original = cipher.doFinal(data)
                String(original, Charset.forName("utf-8"))
            } catch (e: Exception) {
                ";"
            }
        } catch (ex: Exception) {
            ";"
        }
    }

}
