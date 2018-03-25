package org.kreal.lwp

import android.Manifest
import android.app.DialogFragment
import android.app.FragmentManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class StoragePermissionGrant : DialogFragment(), View.OnClickListener {
    override fun onClick(view: View) {
        when (view.id) {
            R.id.go_setting_button -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:" + activity.packageName)
                startActivityForResult(intent, 424)
            }
            R.id.grant_button -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) requestPermissions(permissions, 233)
        }
    }

    interface PermissionGrantListener {
        fun onReject()
        fun onGrant()
    }

    private var mListener: PermissionGrantListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PermissionGrantListener)
            mListener = context
//        else throw ClassCastException(context.toString() + "must implement PermissionGrantListener")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog.setCanceledOnTouchOutside(false)
        val view = inflater.inflate(R.layout.stroge_permisson_grant, container, false)
        view.also {
            it.findViewById<Button>(R.id.go_setting_button).setOnClickListener(this)
            it.findViewById<Button>(R.id.grant_button).setOnClickListener(this)
        }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (checkPermissions(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) context else activity)) {
            dialog.dismiss()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (checkPermissions(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) context else activity)) {
            dialog.dismiss()
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (tag == null || manager.findFragmentByTag(tag) == null)
            return super.show(manager, tag)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val context = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) context else activity)
                ?: return
        if (checkPermissions(context)) {
            mListener?.onGrant()
        } else
            mListener?.onReject()
    }

    companion object {
        private val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)

        fun checkPermissions(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                return true
            permissions.forEach {
                if (context.checkSelfPermission(it) == PackageManager.PERMISSION_DENIED)
                    return false
            }
            return true
        }
    }

}