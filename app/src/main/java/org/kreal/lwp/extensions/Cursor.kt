package org.kreal.lwp.extensions

import android.database.Cursor
import android.util.Log

/**
 * Created by lthee on 2017/10/3.
 */
fun Cursor.Logi() {
    var stringBuild = StringBuilder()
    while (moveToNext()) {
        for (i in 0..(columnCount - 1))
            stringBuild.append(getColumnName(i)).append(":").append(getString(i)).append(" ")
        stringBuild.append("\r\n")
    }
    Log.i("Cursor", stringBuild.toString())

}