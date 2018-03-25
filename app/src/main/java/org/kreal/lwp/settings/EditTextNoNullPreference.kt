package org.kreal.lwp.settings

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.preference.EditTextPreference
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.TranslateAnimation

/**
 * Created by lthee on 2018/3/24.
 * 不允许输入空的EdiTextPreference
 */
class EditTextNoNullPreference(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : EditTextPreference(context, attrs, defStyleAttr, defStyleRes) {

    private val animation: Animation

    init {
        animation = TranslateAnimation(0f, 10f, 0f, 0f)
        animation.duration = 800
        animation.setInterpolator {
            val num = 5
            (Math.sin(it * num * 2 * Math.PI) * (Math.E - Math.exp(it.toDouble()))).toFloat()
        }
    }


    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, android.R.attr.editTextPreferenceStyle)

    constructor(context: Context?) : this(context, null)

    override fun showDialog(state: Bundle?) {
        super.showDialog(state)
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (editText.text.toString() == "")
                editText.startAnimation(animation)
            else {
                dialog.dismiss()
                onDialogClosed(true)
            }
        }
    }
}