package com.manhua.oh.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.util.Base64.DEFAULT
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.Response
import com.manhua.oh.Constant
import com.manhua.oh.OhDatabase
import com.manhua.oh.R
import com.manhua.oh.adapter.ReadAdapter
import com.manhua.oh.bean.Chapter
import com.manhua.oh.bean.Record
import com.manhua.oh.request.CookieRequest
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
            href = chapter.prev
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
                vpComic.currentItem = vpComic.currentItem - 1
            }
        }
        sEnd.setOnClickListener {
            if (chapter.page != 0 && vpComic.currentItem == chapter.page - 1) {
                loadNext()
            } else {
                vpComic.currentItem = vpComic.currentItem + 1
            }
        }

        val vpAdapter = ReadAdapter(getActivity(), bitmaps, R.layout.item_viewpager_comic)
        vpComic.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        vpComic.adapter = vpAdapter
        onPageChangeCb = OnPageChangeCb(this)
        vpComic.registerOnPageChangeCallback(onPageChangeCb)

        val rvAdapter = ReadAdapter(getActivity(), bitmaps, R.layout.item_listview_comic)
        rvComic.layoutManager =
            LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false)
        rvComic.adapter = rvAdapter
        onScrollL = OnScrollL(this)
        rvComic.addOnScrollListener(onScrollL)
    }

    private fun reload() {
        chapter.page = 0
        countLoad = 1
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
            comicActivity.updatePage()
            comicActivity.pbRequest.visibility = View.VISIBLE
            comicActivity.addComic()
            comicActivity.updateRecord(position)
        }
    }

    private class OnScrollL(val comicActivity: ComicActivity) : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                comicActivity.pbRequest.visibility = View.VISIBLE
                comicActivity.addComic()
            }
        }
    }

    private fun updateRecord(position: Int) {
        record.page = position
        record.timestamp = System.currentTimeMillis()
        OhDatabase.db.recordDao().insert(record)
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
        record.userId = OhDatabase.db.getLogin().userId
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

        // 获取页码
        val javascripts = document.select("script")
        var data = ""
        for (javascript in javascripts) {
            val text = javascript.html()
            if (text.contains("C_DATA")) {
                data = text.split("\'")[1];
            }
        }
        var mh = decrypt(data)
        mh = mh.substring(mh.indexOf("{"), mh.indexOf("}") + 1)
        val jsonObject = JSONObject(mh)
        val total = jsonObject["totalimg"].toString().toInt()
        chapter.page = total
        Log.i(TAG, "total = $total")
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.loading_0)
        for (i in 0 until total) {
            bitmaps.add(bitmap)
        }
        vpComic.currentItem = 0
        updatePage()
        updateAdapter()

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

        val prefix = Constant.URL + "/comic" + dataId + URLEncoder.encode(title).replace("+", "%20") + "/"
        chapter.prefix = prefix
        Log.i(TAG, "prefix = $prefix")

        // 初始化列表
        addComic()
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
        pbPage.secondaryProgress = countLoad
    }

    private var countLoad = 1;
    private val maxLoad = 20
    private var countRequest = 0;
    private fun addComic() {
        Log.i(
            TAG,
            "addComic: countLoad = $countLoad, size = ${bitmaps.size} countRequest = $countRequest"
        )

        // 单线程
        if (countRequest >= 1 || countLoad > chapter.page) {
            pbRequest.visibility = View.GONE
            return
        }
        countRequest++

        val src = "${chapter.prefix}${String.format("%04d", countLoad)}.jpg"
        val position = countLoad - 1
        ComicLoader.loadSrc(getActivity(), src, Response.Listener {
            Log.i(TAG, "loadSrc finish = $src $countLoad")
            countRequest--
            runOnUiThread {
                pbRequest.visibility = View.GONE
                updatePage()
            }

                Log.i(TAG, "bitmap.size = ${bitmaps.size} countLoad = $countLoad")
            if (it != null)
                if (position < bitmaps.size)
                    bitmaps[position] = it
                Handler().postDelayed({
                    updateAdapter()
                }, 100)

                Log.i(TAG, "load bitmap = $countLoad - ${vpComic.currentItem}")
                if (countLoad - vpComic.currentItem < maxLoad) {
                    countLoad++
                    addComic()
                }
        })
    }

    private fun decrypt(sSrc: String): String {
        return try {
            val sKey = "JRUIFMVJDIWE569j"
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
