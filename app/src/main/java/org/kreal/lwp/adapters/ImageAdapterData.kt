package org.kreal.lwp.adapters

import android.net.Uri

/**
 * Created by lthee on 2018/3/23.
 * adapterData的接口实现，将数据与是否被选择绑定
 */
class ImageAdapterData(var data: List<Uri>) : AdapterData<Uri, Boolean> {

    val selects: MutableList<Uri> = arrayListOf()

    override fun getSize() = data.size

    override fun getDataState(data: Uri): Boolean = selects.contains(data)

    override fun setDataState(data: Uri, state: Boolean) {
        if (state) selects.add(data) else selects.remove(data)
    }

    override operator fun get(index: Int): Uri = data[index]

}