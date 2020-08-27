package com.manhua.oh.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.manhua.oh.R
import com.manhua.oh.activity.ComicActivity
import kotlinx.android.synthetic.main.activity_comic.*
import kotlinx.android.synthetic.main.fragment_menu.*


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
                comicActivity.updateCurrent(comicActivity.current)
            }
            if(comicActivity.rvComic.visibility == View.VISIBLE)
                comicActivity.ivDirect.rotation = 0f

            val sbPage = view.findViewById<SeekBar>(R.id.sbPage)
            sbPage.max = comicActivity.vpComic.adapter!!.itemCount - 1
            sbPage.progress = comicActivity.vpComic.currentItem
            val onSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    comicActivity.updateCurrent(progress)
                    comicActivity.updateVisible()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

            }
            sbPage.setOnSeekBarChangeListener(onSeekBarChangeListener)
        }

        return view
    }
}