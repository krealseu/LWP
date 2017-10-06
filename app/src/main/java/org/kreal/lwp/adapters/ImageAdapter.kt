package org.kreal.lwp.adapters

import android.content.Context
import android.preference.PreferenceManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_image.view.*
import org.kreal.lwp.R

/**
 * Created by lthee on 2017/10/2.
 */
class ImageAdapter(val context: Context, var data: Array<String>?) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    var imageHeight: Int = PreferenceManager.getDefaultSharedPreferences(context).getInt("test", 400)
    var isSelected = false
    var selects = BooleanArray(data?.size ?: 0)

    public fun swapeData(datas: Array<String>?) {
        data = datas
        selects = BooleanArray(data?.size ?: 0)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ImageViewHolder {
        var view = LayoutInflater.from(parent?.context).inflate(R.layout.item_image, parent, false)
//        var view1 = View.inflate(parent?.context,R.layout.item_image,null)
        return ImageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    override fun onBindViewHolder(holder: ImageViewHolder?, position: Int) {
        holder?.bindData(data?.get(position)!!, position)
    }

    inner class ImageViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        var item_image = itemView!!.item_image
        var checkbox = itemView!!.checkbox

        init {
            item_image.scaleType = ImageView.ScaleType.CENTER_CROP
            item_image.minimumHeight = imageHeight
        }

        fun bindData(data: String, position: Int) {
            Glide.with(context).load(data).into(item_image)
            checkbox.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
            checkbox.isChecked =  selects[position]
            checkbox.setOnCheckedChangeListener { _, ischeckd -> selects[position] = ischeckd }
            itemView.setOnLongClickListener {
                if (isSelected)
                    false
                else {
                    isSelected = true
                    for (i in 0..(selects.size - 1)) {
                        selects[i] = false
                    }
                    selects[position] = true
                    notifyDataSetChanged()
                    true
                }
            }

        }
    }
}