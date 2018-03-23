package org.kreal.lwp.adapters

/**
 * Created by lthee on 2018/3/23.
 * 适配器的数据管理接口，将data的和指定状态绑定，方便在adapter中根据状态，绑定数据
 */
interface AdapterData<DATA, STATE> {
    fun getSize(): Int
    fun getDataState(data: DATA): STATE
    fun setDataState(data: DATA, state: STATE)
    operator fun get(index: Int): DATA
}