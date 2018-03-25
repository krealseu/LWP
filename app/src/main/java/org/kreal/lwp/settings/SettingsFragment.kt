package org.kreal.lwp.settings

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.*
import android.widget.Toast
import org.kreal.lwp.BuildConfig
import org.kreal.lwp.R
import org.kreal.lwp.StoragePermissionGrant
import org.kreal.lwp.backup.BackupService
import java.io.File


class SettingsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private val mBackupRestorePreferenceChangeListener: Preference.OnPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference, newVaule: Any ->
        val name = newVaule.toString()
        return@OnPreferenceChangeListener when (preference.key) {
            Backup -> {
                BackupService.startActionBackup(activity, "$name.lwpbackup")
                true
            }
            Restore -> {
                if (File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "$name.lwpbackup").isFile) {
                    BackupService.startActionRestore(activity, "$name.lwpbackup")
                    true
                } else {
                    Toast.makeText(activity, "Fail: File $name.lwp is't exists.", Toast.LENGTH_SHORT).show()
                    false
                }
            }
            else -> false
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean = when (preference.key) {
        Restore, Backup -> {
            if (!StoragePermissionGrant.checkPermissions(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) context else activity)) {
                (preference as DialogPreference).dialog.dismiss()
                StoragePermissionGrant().show(fragmentManager, "RequestStoragePermission")
            }
            true
        }
        else -> false
    }


    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        val stringValue = newValue.toString()
        if (preference is ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            val index = preference.findIndexOfValue(stringValue)
            // Set the summary to reflect the new value.
            preference.summary = if (index >= 0) preference.entries[index] else null
        } else preference.summary = stringValue
        return true
    }

    private fun bindPreferenceSummaryToValue(preference: Preference) {
        preference.onPreferenceChangeListener = this
        preference.onPreferenceChangeListener.onPreferenceChange(preference, preference.sharedPreferences.getString(preference.key, ""))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_general)
        setHasOptionsMenu(true)

//        findPreference(WallpaperSource).onPreferenceClickListener = this

//        val wallpaperSourcePrf = findPreference(WallpaperSource)
//        wallpaperSourcePrf.summary = preferenceManager.sharedPreferences.getString(wallpaperSourcePrf.key, wallpaperSourcePrf.summary.toString())

        bindPreferenceSummaryToValue(findPreference(RefreshTime))
        bindPreferenceSummaryToValue(findPreference(AnimationTime))
        bindPreferenceSummaryToValue(findPreference(FPSControl))

        findPreference(Backup).onPreferenceClickListener = this
        findPreference(Restore).onPreferenceClickListener = this
        findPreference(Backup).onPreferenceChangeListener = mBackupRestorePreferenceChangeListener
        findPreference(Restore).onPreferenceChangeListener = mBackupRestorePreferenceChangeListener

        findPreference("Version").summary = BuildConfig.VERSION_NAME

//        bindPreferenceSummaryToValue(findPreference(RefreshTime))
//        bindPreferenceSummaryToValue(findPreference(AnimationTime))
//        bindPreferenceSummaryToValue(findPreference("example_list"))
    }

}