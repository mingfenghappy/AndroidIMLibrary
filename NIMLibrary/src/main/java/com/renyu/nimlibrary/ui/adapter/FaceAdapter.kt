package com.renyu.nimlibrary.ui.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.renyu.nimlibrary.R
import com.renyu.nimlibrary.util.emoji.EmojiUtils
import kotlinx.android.synthetic.main.adapter_face.view.*

class FaceAdapter() : RecyclerView.Adapter<FaceAdapter.FaceHolder>() {

    var context: Context? = null
    var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(value: String, drawable: Drawable)
    }

    constructor(context: Context, onItemClickListener: OnItemClickListener) : this() {
        this.context = context
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaceHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.adapter_face, parent, false)
        return FaceHolder(view)
    }

    override fun getItemCount() = EmojiUtils.getDisplayCount()

    override fun onBindViewHolder(holder: FaceHolder, position: Int) {
        holder.showImage(position)
    }

    inner class FaceHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun showImage(position: Int) {
            itemView.iv_face_item.setImageDrawable(EmojiUtils.getDisplayDrawable(context, position))
            itemView.iv_face_item.setOnClickListener {
                onItemClickListener?.onItemClick(EmojiUtils.getDisplayText(position), EmojiUtils.getDisplayDrawable(context, position))
            }
        }
    }
}