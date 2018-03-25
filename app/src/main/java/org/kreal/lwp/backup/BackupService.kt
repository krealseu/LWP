package org.kreal.lwp.backup

import android.app.IntentService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast

/**
 * An [BackupService] subclass for handling asynchronous data Backup & Restore requests in
 * a service on a separate handler thread.
 */
class BackupService : IntentService("BackupService") {

    //确保一次只进行一次备份和恢复任务，不允许等待下一个action
    private var isDoing = false

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            val name = intent.getStringExtra(EXTRA_NAME)
            if (ACTION_BACKUP == action) {
                handleActionBackup(name)
            } else if (ACTION_RESTORE == action) {
                handleActionRestore(name)
            }
        }
    }

    /**
     * Handle action Backup in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionBackup(name: String) {
        showMsg("Backup is doing, Please wait")
        BackupManager(baseContext).backup(name)
        showMsg("Finish backup to $name")
        isDoing = false
    }

    /**
     * Handle action Restore in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionRestore(name: String) {
        showMsg("Restore is doing, Please wait")
        if (BackupManager(baseContext).restore(name))
            showMsg("Finish restore from $name")
        else
            showMsg("Fail restore from $name")
        isDoing = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return if (isDoing) {
            Toast.makeText(baseContext, "Please wait ！last backup task is doing", Toast.LENGTH_SHORT).show()
            Service.START_NOT_STICKY
        } else {
            isDoing = true
            super.onStartCommand(intent, flags, startId)
        }
    }

    private fun showMsg(msg: String) {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val ACTION_BACKUP = "org.kreal.lwp.backup.action.BACKUP"
        private const val ACTION_RESTORE = "org.kreal.lwp.backup.action.RESTORE"

        private const val EXTRA_NAME = "org.kreal.lwp.backup.extra.PARAM1"

        /**
         * Starts this service to perform action Backup with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        fun startActionBackup(context: Context, name: String) {
            val intent = Intent(context, BackupService::class.java)
            intent.action = ACTION_BACKUP
            intent.putExtra(EXTRA_NAME, name)
            context.startService(intent)
        }

        /**
         * Starts this service to perform action Restore with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        fun startActionRestore(context: Context, name: String) {
            val intent = Intent(context, BackupService::class.java)
            intent.action = ACTION_RESTORE
            intent.putExtra(EXTRA_NAME, name)
            context.startService(intent)
        }
    }
}
