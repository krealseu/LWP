package org.kreal.lwp.share

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.NotificationCompat
import android.util.Log
import org.kreal.lwp.R
import org.kreal.lwp.backup.BackupManager
import org.kreal.lwp.models.WallpaperManager
import org.kreal.lwp.settings.WallpaperSource
import java.io.File

class ShareDealService : IntentService("ShareDealService") {

    override fun onHandleIntent(intent: Intent?) {
        intent?.also {
            if (it.action == DealAction) {
                if (it.type == Image) {
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
                            .setContentTitle("LWP Copy Image")
                            .setContentText("total:${uriList.size} success:$result fail:${uriList.size - result}")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setAutoCancel(true)
                            .build()
                    val nm = baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.notify("LWPShareDeal", 233, notification)
                } else if (it.type == Zip) {
                    it.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM).forEach {
                        BackupManager(baseContext).restoreFromUri(it)
                    }
                    val notification = NotificationCompat.Builder(baseContext, "LWPShareDeal")
                            .setContentTitle("LWP load info from Zip")
                            .setContentText("Complete")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setAutoCancel(true)
                            .build()
                    val nm = baseContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    nm.notify("LWPShareDeal", 233, notification)
                }
            }
        }
    }

    companion object {
        private const val DealAction = "org.kreal.lwp.shearDeal"
        const val Image = "image"
        const val Zip = "Zip"
        fun startAction(context: Context, param: ArrayList<Uri>, type: String) {
            val intent = Intent(context, ShareDealService::class.java)
            intent.action = DealAction
            intent.type = type
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, param)
            context.startService(intent)
        }
    }

}
