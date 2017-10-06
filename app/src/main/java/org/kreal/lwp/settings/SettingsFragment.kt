package org.kreal.lwp.settings

import android.os.Bundle
import android.preference.*
import org.kreal.lwp.R

/**
 * Created by lthee on 2017/10/1.
 */
class SettingsFragment : PreferenceFragment() {
    private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
        val stringValue = value.toString()
        if (preference is EditTextPreference) {
            preference.setSummary(stringValue)
        } else if (preference is ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            val index = preference.findIndexOfValue(stringValue)

            // Set the summary to reflect the new value.
            preference.setSummary(
                    if (index >= 0)
                        preference.entries[index]
                    else
                        null)
        }
        true
    }

    private fun bindPreferenceSummaryToValue(preference: Preference) {
        // Set the listener to watch for value changes.
        preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.context)
                        .getString(preference.key, ""))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_general)
        setHasOptionsMenu(true)
        bindPreferenceSummaryToValue(findPreference(RefreshTime))
        bindPreferenceSummaryToValue(findPreference(AnimationTime))
//        bindPreferenceSummaryToValue(findPreference("example_list"))
    }
}