package com.renyu.nimlibrary.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.bean.ObserveResponse
import com.renyu.nimlibrary.bean.ObserveResponseType
import com.renyu.nimlibrary.ui.adapter.FaceAdapter
import com.renyu.nimlibrary.util.RxBus
import com.renyu.nimlibrary.util.emoji.EmojiUtils
import kotlinx.android.synthetic.main.fragment_emoji.*

class EmojiFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_emoji, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val faceAdapter = FaceAdapter(context!!, object : FaceAdapter.OnItemClickListener {
            override fun onItemClick(value: String, drawable: Drawable) {
                RxBus.getDefault().post(ObserveResponse(EmojiUtils.getEmojiSpannableString(value, drawable), ObserveResponseType.Emoji))
            }
        })

        rv_emoji.setHasFixedSize(true)
        rv_emoji.layoutManager = GridLayoutManager(context, 7)
        rv_emoji.adapter = faceAdapter
    }
}