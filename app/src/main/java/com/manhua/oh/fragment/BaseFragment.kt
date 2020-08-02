package com.manhua.oh.fragment

import androidx.fragment.app.Fragment
import com.manhua.oh.database.OhDatabase

open class BaseFragment : Fragment() {
    public val TAG = javaClass.simpleName;
    var user = OhDatabase.db.getLogin()
}