package org.kreal.lwp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import org.kreal.lwp.service.LWPService

class ChangeWallpaperActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(baseContext).sendBroadcast(Intent(LWPService.ChangeWallpaperIntent))
        finish()
    }
}
