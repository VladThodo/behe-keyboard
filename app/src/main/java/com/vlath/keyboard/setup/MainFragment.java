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
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;



@EFragment(R.layout.main)
public class MainFragment extends Fragment {

    @Click(R.id.select)
    void mSelectButton(){
        Context mContext = null;
        try { mContext = getActivity().getApplicationContext(); }
        catch(Exception e) {  Log.e("NULL", "Context is null"); }
        if(mContext != null) {
            InputMethodManager imeManager = (InputMethodManager)
                    getActivity().getApplicationContext().
                            getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imeManager != null) {
                imeManager.showInputMethodPicker();
            } else {
                Toast.makeText(getActivity(), "Not possible", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Click(R.id.enable)
    void mEnableButton(){
        this.startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
    }
}