package com.manhua.oh.activity

import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.manhua.oh.OhDatabase
import com.manhua.oh.R

open class BaseActivity : AppCompatActivity() {

    fun getActivity() = this

    val TAG = javaClass.simpleName

    override fun setContentView(layoutResID: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 如果亮色，设置状态栏文字为黑色
            if (resources.getColor(R.color.colorPrimaryDark, null) == resources.getColor(
                            R.color.white, null)) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }

        super.setContentView(layoutResID)
    }

    var user = OhDatabase.db.getLogin()

}
