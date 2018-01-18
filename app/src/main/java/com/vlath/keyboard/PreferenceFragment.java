package com.vlath.keyboard;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference;

/**
 * Created by todo on 30.06.2017.
 */

public class PreferenceFragment extends android.preference.PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    ListPreference listTheme;
    ListPreference listStart;
    @Override
    public void onCreate(Bundle s){
        super.onCreate(s);
        addPreferencesFromResource(R.xml.ime_preferences);
        listTheme = (ListPreference) findPreference("theme");
        listStart = (ListPreference) findPreference("start");
        listTheme.setSummary(listTheme.getEntry());
        listStart.setSummary(listStart.getEntry());
        PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext()).registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        listTheme.setSummary(listTheme.getEntry());
        listStart.setSummary(listStart.getEntry());
    }
}
