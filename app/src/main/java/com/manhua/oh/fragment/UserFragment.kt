package com.manhua.oh.fragment

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.android.volley.Request
import com.android.volley.Response
import com.google.android.material.snackbar.Snackbar
import com.manhua.oh.Constant
import com.manhua.oh.OhDatabase
import com.manhua.oh.R
import com.manhua.oh.request.LoginRequest
import com.manhua.oh.request.VerifyRequest
import com.manhua.oh.tool.ComicLoader
import com.manhua.oh.tool.VolleyQueue
import kotlinx.android.synthetic.main.fragment_user.view.*
import org.json.JSONObject

/**
 * A simple [Fragment] subclass.
 */
class UserFragment : BaseFragment() {
    private lateinit var root: View

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_user, container, false)

        initView()
        initData()

        return root
    }

    private fun initView() {
    }

    private fun initData() {
        val user = OhDatabase.db.getLogin()
        val cookie = user.cookie
        Log.i(TAG, "cookie = $cookie")
        if (cookie.isEmpty()) {
            root.llSignIn.visibility = View.VISIBLE
            updateVerify()
            root.fabLogin.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
            root.fabLogin.setOnClickListener { login() }
            root.fabLogin.setImageResource(R.mipmap.icon_enter)
        } else {
            root.llSignIn.visibility = View.INVISIBLE
            val src = Constant.URL + "/user/image/" + user.userId
            ComicLoader.loadSrc(activity as Context, src, root.ivHead)
            root.tvName.text = user.nickName
            root.fabLogin.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.red))
            root.fabLogin.setOnClickListener { logOut() }
            root.fabLogin.setImageResource(R.mipmap.icon_error)
        }
    }


    private var verifyId = ""
    private fun updateVerify() {
        val url = "https://www.ohmanhua.com/dynamic/kaptcha?v=" + System.currentTimeMillis()
        val imageRequest = VerifyRequest(url, Response.Listener {
            resources.displayMetrics.scaledDensity
            Log.i(TAG, "smallDp = ")
            root.ivVerify.setImageBitmap(it)
        }, 0, 0, ImageView.ScaleType.FIT_XY, Bitmap.Config.ARGB_8888, Response.ErrorListener() {
            Log.e(TAG, it.message)
        }, Response.Listener {
            verifyId = it
        })

        VolleyQueue.addRequest(imageRequest)
    }

    private fun login() {
        val url = "https://www.ohmanhua.com/user/loginHandle?v=" + System.currentTimeMillis()

        val params = HashMap<String, String>()
        params.put("user_name", root.etUsername.text.toString())
        params.put("user_pwd", root.etPassword.text.toString())
        params.put("verifyCode", root.etVerify.text.toString())
        params.put("remeberModule", "30")

        val header = HashMap<String?, String?>()
        header.put("cookie", "KAPTCHA_ID=$verifyId")

        val formRequest =
                LoginRequest(Request.Method.POST, url, params, header, Response.Listener<String> {
                    Log.i(TAG, "listener $it")
                    val jsonObject = JSONObject(it)
                    if (jsonObject.getString("status") == "S") {
                        initData()
                    } else {
                        Snackbar.make(root.fabLogin, jsonObject.getString("message"), Snackbar.LENGTH_LONG).show()
                    }

                }, Response.ErrorListener {
                    Log.e(TAG, "errorListener ${it.message}")
                }, Response.Listener {
                    Log.i(TAG, "cookies " + it.toString())
                    it.username = params["user_name"]!!
                    it.password = params["user_pwd"]!!
                    OhDatabase.db.userDao().insert(it)
                })

        VolleyQueue.addRequest(formRequest)
    }

    private fun logOut() {
        val user = OhDatabase.db.getLogin()
        user.cookie = ""
        OhDatabase.db.userDao().update(user)

        initData()
    }
}
