package org.kreal.lwp.settings

import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import org.kreal.lwp.R
import org.kreal.widget.filepickdialog.FilePickDialogFragment


class SettingsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    override fun onPreferenceClick(preference: Preference): Boolean {
        if (preference.key == WallpaperSource) {
            FilePickDialogFragment().apply {
                selectFolder = true
                setListener {
                    val path = it[0].path
                    preference.summary = path
                    preference.sharedPreferences.edit().putString(preference.key, path).apply()
                }
            }.show(fragmentManager, preference.key)
            return true
        }
        return false
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

        findPreference(WallpaperSource).onPreferenceClickListener = this

        val wallpaperSourcePrf = findPreference(WallpaperSource)
        wallpaperSourcePrf.summary = preferenceManager.sharedPreferences.getString(wallpaperSourcePrf.key, wallpaperSourcePrf.summary.toString())

        bindPreferenceSummaryToValue(findPreference(RefreshTime))
        bindPreferenceSummaryToValue(findPreference(AnimationTime))
        bindPreferenceSummaryToValue(findPreference(FPSControl))

//        bindPreferenceSummaryToValue(findPreference(RefreshTime))
//        bindPreferenceSummaryToValue(findPreference(AnimationTime))
//        bindPreferenceSummaryToValue(findPreference("example_list"))
    }
}