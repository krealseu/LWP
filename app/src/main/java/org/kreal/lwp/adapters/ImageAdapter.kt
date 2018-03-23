package org.kreal.lwp.adapters

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import com.bumptech.glide.Glide
import org.kreal.lwp.R

/**
 * Created by lthee on 2018/3/23.
 * image的适配器，将数据和view绑定
 */

class ImageAdapter(private var data: AdapterData<Uri, Boolean>, private val imageHeight: Int) : RecyclerView.Adapter<ImageAdapter.ItemHolder>() {

    private var mListener: OnItemClickListener? = null

    private var mOnLongListener: OnItemLongClickListener? = null


    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        mOnLongListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ItemHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.item_image, parent, false)
        val itemHolder = ItemHolder(view)
        itemHolder.apply {
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.minimumHeight = imageHeight
            itemView.setOnClickListener {
                mListener?.onItemClick(this, data[adapterPosition], adapterPosition)
            }
            itemView.setOnLongClickListener {
                return@setOnLongClickListener mOnLongListener?.onItemLongClick(this, data[adapterPosition], adapterPosition)
                        ?: false
            }

        }
        return itemHolder
    }

    override fun getItemCount(): Int = data.getSize()

    override fun onBindViewHolder(holder: ItemHolder?, position: Int) {
        holder?.apply {
            Glide.with(holder.itemView).load(data[adapterPosition]).into(imageView)
            shadow.visibility = if (data.getDataState(data[adapterPosition])) View.VISIBLE else View.INVISIBLE
        }
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_image)
        val shadow: View = itemView.findViewById(R.id.item_shadow)
        fun showOrHideShadow(visible: Boolean) {
            shadow.visibility = if (visible) View.VISIBLE else View.INVISIBLE
            val annotation = if (visible) AlphaAnimation(0f, 1f) else AlphaAnimation(1f, 0f)
            annotation.duration = 200
            shadow.startAnimation(annotation)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(itemHolder: ItemHolder, data: Uri, position: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(itemHolder: ItemHolder, data: Uri, position: Int): Boolean
    }
}