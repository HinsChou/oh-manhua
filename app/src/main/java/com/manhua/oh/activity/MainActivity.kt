package com.manhua.oh.activity

import android.Manifest
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.manhua.oh.Constant
import com.manhua.oh.R
import com.manhua.oh.fragment.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initData()
    }

    private fun initView() {

        fabMain.setOnClickListener {
            showFragment(mainFragment)
            fabMain.setImageResource(R.mipmap.icon_home_solid)
            fabMain.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.blue))
        }
        fabMain.setOnLongClickListener {
            mainFragment.requestMain()
            true
        }

        ivLike.setOnClickListener {
            showFragment(likeFragment)
            ivLike.setImageResource(R.mipmap.icon_like_solid)
            ivLike.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.red))
        }
        ivUser.setOnClickListener {
            showFragment(userFragment)
            ivUser.setImageResource(R.mipmap.icon_my_solid)
            ivUser.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.green))
        }
        ivSearch.setOnClickListener {
            showFragment(searchFragment)
            ivSearch.setImageResource(R.mipmap.icon_tag_solid)
            ivSearch.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.orange))
        }
    }

    private fun showFragment(fragment: BaseFragment) {
        supportFragmentManager.beginTransaction().hide(lastFragment).show(fragment).commit()
        lastFragment = fragment

        fabMain.setImageResource(R.mipmap.icon_home_stroke)
        fabMain.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.colorSub))
        ivLike.setImageResource(R.mipmap.icon_like_stroke)
        ivLike.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.colorSub))
        ivUser.setImageResource(R.mipmap.icon_my_stroke)
        ivUser.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.colorSub))
        ivSearch.setImageResource(R.mipmap.icon_tag_stroke)
        ivSearch.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.colorSub))
    }

    private val mainFragment = MainFragment()
    val likeFragment = LikeFragment()
    val userFragment = UserFragment()
    private val searchFragment = SearchFragment()

    private var lastFragment: BaseFragment = mainFragment
    private fun initData() {
        // 请求权限
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(getActivity(), permissions, Constant.REQUEST_PERMISSION_WRITE)

        supportFragmentManager.beginTransaction()
                .add(R.id.flMain, mainFragment)
                .hide(mainFragment)
                .add(R.id.flMain, likeFragment)
                .hide(likeFragment)
                .add(R.id.flMain, userFragment)
                .hide(userFragment)
                .add(R.id.flMain, searchFragment)
                .hide(searchFragment)

                .show(lastFragment)
                .commit()
    }
}
