package com.manhua.oh.fragment

import androidx.fragment.app.Fragment
import com.manhua.oh.OhDatabase

open class BaseFragment : Fragment() {
    public val TAG = javaClass.simpleName;
    val user = OhDatabase.db.getLogin()
}