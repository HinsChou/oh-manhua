package com.manhua.oh.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.manhua.oh.R
import com.manhua.oh.activity.ComicActivity


class MenuFragment : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogBottom)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View =
            inflater.inflate(R.layout.fragment_menu, container, false)

        if (activity is ComicActivity) {
            val comicActivity = activity as ComicActivity
            val ivPrev = view.findViewById<ImageView>(R.id.ivPrev)
            ivPrev.setOnClickListener {
                comicActivity.loadPrev()
                dismiss()
            }
            val ivNext = view.findViewById<ImageView>(R.id.ivNext)
            ivNext.setOnClickListener {
                comicActivity.loadNext()
                dismiss()
            }

            val ivDirect = view.findViewById<ImageView>(R.id.ivDirect)
            ivDirect.setOnClickListener {
                val direct = comicActivity.toggleDirect()
                ivDirect.rotation = 0f
                if (direct == RecyclerView.HORIZONTAL)
                    ivDirect.rotation = 90f
            }
        }

        return view
    }
}