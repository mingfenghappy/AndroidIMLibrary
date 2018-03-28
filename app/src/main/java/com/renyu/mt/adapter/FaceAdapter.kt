package com.renyu.mt.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.renyu.mt.R
import com.renyu.mt.utils.FaceIconUtil
import kotlinx.android.synthetic.main.adapter_face.view.*
import org.jetbrains.anko.imageResource

class FaceAdapter() : RecyclerView.Adapter<FaceAdapter.FaceHolder>() {

    var context: Context? = null
    var iconfaceRsId: IntArray? = null
    var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(value: String, res: Int)
    }

    @JvmOverloads constructor(context: Context, iconfaceRsId:IntArray = FaceIconUtil.getInstance().faceIconRsId, onItemClickListener: OnItemClickListener) : this() {
        this.context = context
        this.iconfaceRsId = iconfaceRsId
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): FaceHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.adapter_face, parent, false)
        return FaceHolder(view)
    }

    override fun getItemCount() = iconfaceRsId!!.size

    override fun onBindViewHolder(holder: FaceHolder?, position: Int) {
        holder?.showImage(position)
    }

    inner class FaceHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun showImage(position: Int) {
            itemView.iv_face_item.imageResource = iconfaceRsId!![position]
            itemView.iv_face_item.setOnClickListener {
                onItemClickListener!!.onItemClick(FaceIconUtil.getInstance().getFaceKey(position), iconfaceRsId!![position])
            }
        }
    }
}