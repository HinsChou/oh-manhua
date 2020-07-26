package com.manhua.oh.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
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
import com.manhua.oh.tool.VolleyQueue
import kotlinx.android.synthetic.main.activity_comic.*
import org.jsoup.Jsoup
import java.net.URLEncoder

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

    private val bitmaps = ArrayList<Bitmap>()
    private fun initView() {
        ivBack.setOnClickListener { finish() }
        sStart.setOnClickListener {
            if (vpComic.currentItem == 0) {
                if (chapter.prev.isEmpty()) {

                } else {
                    href = chapter.prev
                    reload()
                }
            } else {
                vpComic.currentItem = vpComic.currentItem - 1
            }
        }
        sEnd.setOnClickListener {
            if (chapter.page != 0 && vpComic.currentItem == chapter.page - 1) {
                if (chapter.next.isEmpty()) {

                } else {
                    href = chapter.next
                    reload()
                }
            } else {
                vpComic.currentItem = vpComic.currentItem + 1
            }
        }

        val readAdapter = ReadAdapter(getActivity(), bitmaps)

        vpComic.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        vpComic.adapter = readAdapter
        onPageChangeCb = OnPageChangeCb(this)
        vpComic.registerOnPageChangeCallback(onPageChangeCb)

        rvComic.layoutManager =
            LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false)
        rvComic.adapter = readAdapter
        onScrollL = OnScrollL(this)
        rvComic.addOnScrollListener(onScrollL)
    }

    private fun reload() {
        chapter.page = 0
        countLoad = 1
        vpComic.currentItem = 0
        bitmaps.clear()
        vpComic.adapter?.notifyDataSetChanged()
        rvComic.adapter?.notifyDataSetChanged()
        updatePage()
        initData()
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

        var ids = href.replace(Constant.URL, "").split("/")
        val dataId = ids[1]
        val chapterId = ids[3].replace(".html", "")

        record = OhDatabase.db.getRecordChapter(dataId, chapterId)
        record.chapterId = chapterId
        record.dataId = dataId
        record.userId = OhDatabase.db.getLogin().userId

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

    private val chapter = Chapter()
    private fun handleDetail(html: String) {
        val document = Jsoup.parse(html)
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
    }

    private var countLoad = 1;
    private val maxLoad = 20
    private var countRequest = 0;
    private fun addComic() {
        val src = "${chapter.prefix}${String.format("%04d", countLoad)}.jpg"
        Log.i(
            TAG,
            "addComic: countLoad = $countLoad, size = ${bitmaps.size} countRequest = $countRequest"
        )

        // 单线程
        if (countRequest >= 1 || (chapter.page != 0 && countLoad >= chapter.page)) {
            pbRequest.visibility = View.GONE
            return
        }
        countRequest++

        ComicLoader.loadSrc(getActivity(), src, Response.Listener {
            Log.i(TAG, "loadSrc finish = $src $countLoad")
            countRequest--
            runOnUiThread {
                pbRequest.visibility = View.GONE
                updatePage()
            }

            if (it == null) {
                countLoad--
                chapter.page = countLoad
                updatePage()
                Log.i(TAG, "load end = ${chapter.page}")
            } else {
                Log.i(TAG, "bitmap.size = ${bitmaps.size} countLoad = $countLoad")

                if (countLoad <= bitmaps.size) {
                    bitmaps[countLoad - 1] = it
                } else {
                    bitmaps.add(it)
                }
                Handler().postDelayed({
                    vpComic.adapter?.notifyDataSetChanged()
                    rvComic.adapter?.notifyDataSetChanged()
                }, 100)

                Log.i(TAG, "load bitmap = $countLoad - ${vpComic.currentItem}")
                if (countLoad - vpComic.currentItem < maxLoad) {
                    countLoad++
                    addComic()
                }
            }
        })
    }

}
