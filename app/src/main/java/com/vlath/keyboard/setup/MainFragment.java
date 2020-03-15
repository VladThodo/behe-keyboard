/* Copyright (C) Vlad Todosin 2020 */


package com.vlath.keyboard.setup;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.vlath.keyboard.R;
import com.vlath.keyboard.latin.settings.InputMethodSettingsActivity;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;



@EFragment(R.layout.main)
public class MainFragment extends Fragment {

    @Click(R.id.select)
    void mSelectButton() {
        InputMethodManager imeManager = null;
        try {
            imeManager = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        } catch (Exception e) {
            Log.e("Error", "NullPointer");
        }
        if (imeManager != null) {
            imeManager.showInputMethodPicker();
        } else {
            Toast.makeText(getActivity(), "Not possible", Toast.LENGTH_LONG).show();
        }
    }

    @Click(R.id.enable)
    void mEnableButton(){
        this.startActivity(new Intent(getActivity(), SettingsActivity_.class));
    }
}