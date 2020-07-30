package com.manhua.oh

import android.app.Application
import com.manhua.oh.tool.VolleyQueue

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        OhDatabase.init(applicationContext)
        VolleyQueue.init(applicationContext)
        widthPixels = resources.displayMetrics.widthPixels
    }

    companion object {
        var widthPixels = 0
    }
}