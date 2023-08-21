package de.pme.collector.view.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import de.pme.collector.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}