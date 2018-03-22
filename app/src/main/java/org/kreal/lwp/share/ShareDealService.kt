package org.kreal.lwp.share

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.NotificationCompat
import org.kreal.lwp.R
import org.kreal.lwp.models.WallpaperManager
import org.kreal.lwp.settings.WallpaperSource
import java.io.File

class ShareDealService : IntentService("ShareDealService") {

    override fun onHandleIntent(intent: Intent?) {
        intent?.also {
            if (it.action == dealAction) {
                val file = File(baseContext.filesDir, WallpaperSource)
                if (!file.exists())
                    file.mkdirs()
                else if (file.isFile) {
                    file.delete()
                    file.mkdirs()
                }
                val wallpaperManager = WallpaperManager(File(baseContext.filesDir, WallpaperSource))
                val uriList = it.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                var result = 0
                uriList.forEach {
                    try {
                        wallpaperManager.add(baseContext, it)
                        result++
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                val notification = NotificationCompat.Builder(baseContext, "LWPShareDeal")
                        .setContentTitle("LWP Copy")
                        .setContentText("total:${uriList.size} success:$result fail:${uriList.size - result}")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setAutoCancel(true)
                        .build()
                val nm = baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify("LWPShareDeal", 233, notification)
            }
        }
    }

    companion object {
        private const val dealAction = "org.kreal.lwp.shearDeal"
        fun startAction(context: Context, param: ArrayList<Uri>) {
            val intent = Intent(context, ShareDealService::class.java)
            intent.action = dealAction
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, param)
            context.startService(intent)
        }
    }

}
