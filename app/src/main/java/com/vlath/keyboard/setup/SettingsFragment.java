package com.vlath.keyboard.setup;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.vlath.keyboard.R;
import com.vlath.keyboard.keyboard.KeyboardLayoutSet;
import com.vlath.keyboard.latin.AudioAndHapticFeedbackManager;
import com.vlath.keyboard.latin.RichInputMethodManager;
import com.vlath.keyboard.latin.settings.Settings;

import org.androidannotations.annotations.EFragment;

@org.androidannotations.annotations.PreferenceScreen(R.xml.prefs)
@EFragment
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.prefs);


        final Resources res = getResources();
        final Context context = getActivity();

        // When we are called from the Settings application but we are not already running, some
        // singleton and utility classes may not have been initialized.  We have to call
        // initialization method of these classes here. See {@link LatinIME#onCreate()}.

        RichInputMethodManager.init(context);

        if (!AudioAndHapticFeedbackManager.getInstance().hasVibrator()) {
            removePreference(Settings.PREF_VIBRATE_ON, getPreferenceScreen());
        }
        if (!Settings.readFromBuildConfigIfToShowKeyPreviewPopupOption(res)) {
            removePreference(Settings.PREF_POPUP_ON, getPreferenceScreen());
        }


        SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
                SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                          String key) {
                                   }
                };
        
    }


    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setPadding(0, 0, 0, 0);
        setDivider(new ColorDrawable(Color.TRANSPARENT));
        setDividerHeight(0);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        final Resources res = getResources();
        if (s.equals(Settings.PREF_POPUP_ON)) {
            setPreferenceEnabled(Settings.PREF_KEY_PREVIEW_POPUP_DISMISS_DELAY,
                    Settings.readKeyPreviewPopupEnabled(sharedPreferences, res), getPreferenceScreen());
        }
        if (s.equals(Settings.PREF_POPUP_ON)) {

        }
        if (s.equals(Settings.PREF_HIDE_SPECIAL_CHARS) ||
                s.equals(Settings.PREF_SHOW_NUMBER_ROW))
            KeyboardLayoutSet.onKeyboardThemeChanged();

    }



    static void setPreferenceEnabled(final String prefKey, final boolean enabled,
                                     final PreferenceScreen screen) {
        final androidx.preference.Preference preference = screen.findPreference(prefKey);
        if (preference != null) {
            preference.setEnabled(enabled);
        }
    }

    final SharedPreferences getSharedPreferences() {
        return getPreferenceManager().getSharedPreferences();
    }


    static void removePreference(final String prefKey, final PreferenceScreen screen) {
        final androidx.preference.Preference preference = screen.findPreference(prefKey);
        if (preference != null) {
            screen.removePreference(preference);
        }
    }
}

