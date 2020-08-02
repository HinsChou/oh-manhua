package com.manhua.oh.fragment

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.android.volley.Request
import com.android.volley.Response
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.manhua.oh.database.OhDatabase
import com.manhua.oh.R
import com.manhua.oh.activity.MainActivity
import com.manhua.oh.request.LoginRequest
import com.manhua.oh.request.VerifyRequest
import com.manhua.oh.tool.Snack
import com.manhua.oh.tool.VolleyQueue
import kotlinx.android.synthetic.main.fragment_login.view.*


class LoginFragment : BottomSheetDialogFragment() {

    private val TAG = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogBottom)
    }

    private lateinit var root : View
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_login, container, false)
        updateVerify()
        root.bLogin.setOnClickListener{ login() }
        return root
    }


    private var verifyId = ""
    private fun updateVerify() {
        val url = "https://www.ohmanhua.com/dynamic/kaptcha?t=" + System.currentTimeMillis()
        val imageRequest = VerifyRequest(url, Response.Listener {
            root.ivVerify.setImageBitmap(it)
        }, 0, 0, ImageView.ScaleType.FIT_XY, Bitmap.Config.ARGB_8888, Response.ErrorListener() {
            Log.e(TAG, it.message)
        }, Response.Listener {
            verifyId = it
        })

        VolleyQueue.addRequest(imageRequest)
    }

    private fun login() {
        val url = "https://www.ohmanhua.com/user/loginHandle?t=" + System.currentTimeMillis()

        val params = HashMap<String, String>()
        params["user_name"] = root.etUsername.text.toString()
        params["user_pwd"] = root.etPassword.text.toString()
        params["verifyCode"] = root.etVerify.text.toString()
        params["remeberModule"] = "30"

        val header = HashMap<String, String>()
        header["cookie"] = "KAPTCHA_ID=$verifyId"

        val formRequest =
            LoginRequest(Request.Method.POST, url, params, header, Response.Listener {
                Log.i(TAG, "listener $it")
                if (it.status == "S") {
                    if(activity is MainActivity){
                        val mainActivity = activity as MainActivity
                        mainActivity.userFragment.initData()
                        mainActivity.likeFragment.initData()
                    }
                    dismiss()
                } else {
                    updateVerify()
                    Snack.show(activity as Context, it.message)
                }
            }, Response.ErrorListener {
                Log.e(TAG, "errorListener ${it.message}")
            }, Response.Listener {
                Log.i(TAG, "cookies " + it.toString())
                it.username = params["user_name"]!!
                it.password = params["user_pwd"]!!
                OhDatabase.db.ohDao().insertUser(it)
            })

        VolleyQueue.addRequest(formRequest)
    }
}