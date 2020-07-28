package com.manhua.oh.tool

import android.app.Activity
import android.content.Context
import com.google.android.material.snackbar.Snackbar

class Snack {

    companion object {
        fun show(context: Context, text: String) {
            val activity = (context as Activity)
            activity.runOnUiThread {
                Snackbar.make(activity.window.decorView, text, Snackbar.LENGTH_LONG).show()
            }
        }
    }

}