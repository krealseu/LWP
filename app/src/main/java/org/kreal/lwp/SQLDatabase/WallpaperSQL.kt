package org.kreal.lwp.SQLDatabase

/**
 * Created by lthee on 2017/10/3.
 */
class WallpaperSQL {
    companion object {
        val SQL_Name = "WallpaperManager"
        val SQL_TABLE = "wallpapers"
        val SQLVersion = 1

        val ID = "_id"
        val DATA = "_data"
        val WIDTH = "_width"
        val HEIGHT = "_height"
        val TRANSALTIONX = "_tx"
        val TRANSALTIONY = "_ty"
        val SCALE = "_scale"
        val PERSPECTIVE_SCALE = "_ps"
        val OFFSET = "_offset"

        val createSQL = "CREATE TABLE ${SQL_TABLE}(" +
                "${ID} INTEGER PRIMARY KEY ," +
                "${DATA} TEXT UNIQUE , " +
                "${WIDTH} INTEGER , " +
                "${HEIGHT} INTEGER , " +
                "${TRANSALTIONX} REAL DEFAULT 0 ," +
                "${TRANSALTIONY} REAL DEFAULT 0 ," +
                "${SCALE} REAL DEFAULT 1 ," +
                "${PERSPECTIVE_SCALE} REAL DEFAULT 1 , " +
                "${OFFSET} REAL " +
                ")"

    }
}