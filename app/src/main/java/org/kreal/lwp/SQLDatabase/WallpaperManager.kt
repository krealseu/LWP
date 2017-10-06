package org.kreal.lwp.SQLDatabase

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.BitmapFactory

/**
 * Created by lthee on 2017/10/3.
 */
class WallpaperManager(val context: Context) {

    val wallpapersSQL = SQLCreater(context, WallpaperSQL.SQL_Name, null, WallpaperSQL.SQLVersion)

    public fun insert(path: String) {
        var bitmap = BitmapFactory.decodeFile(path)
        var contentValues = ContentValues()
        contentValues.put(WallpaperSQL.DATA, path)
        contentValues.put(WallpaperSQL.WIDTH, bitmap.width)
        contentValues.put(WallpaperSQL.HEIGHT, bitmap.height)
        contentValues.put(WallpaperSQL.TRANSALTIONX, 0)
        contentValues.put(WallpaperSQL.TRANSALTIONY, 0)
        contentValues.put(WallpaperSQL.SCALE, 1f)
        contentValues.put(WallpaperSQL.PERSPECTIVE_SCALE, 1f)
        contentValues.put(WallpaperSQL.OFFSET, 0)
        var db = wallpapersSQL.writableDatabase
        db.insert(WallpaperSQL.SQL_TABLE, null, contentValues)
    }

    fun getWallpaperRandom(): String {
        var db = wallpapersSQL.readableDatabase
//        var cursor = db.query(WallpaperSQL.SQL_TABLE, Array(1) { WallpaperSQL.DATA }, "_id = ?", Array(1){"1 "}, null, null, "RANDOM()", "1")
        var cursor = db.query(WallpaperSQL.SQL_TABLE, Array(1) { WallpaperSQL.DATA }, null, null, null, null, "RANDOM()", "1")
        cursor.moveToFirst()
        val result = cursor.getString(0)
//        db.close()
        cursor.close()
        return result
    }

    fun query(columns: Array<String>, selection: String?, selectionArgs: Array<String>?, groupBy: String?, having: String?, orderBy: String?): Cursor {
        return wallpapersSQL.readableDatabase.query(WallpaperSQL.SQL_TABLE, columns, selection, selectionArgs, groupBy, having, orderBy)
    }

    class SQLCreater(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context, name, factory, version) {

        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL(WallpaperSQL.createSQL)
        }

        override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {

        }
    }

}