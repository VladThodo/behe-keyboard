package com.vlath.keyboard.setup;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;

import com.vlath.keyboard.R;

import java.util.Objects;

public class MainFragment extends Fragment {

    EditText text;

    private Button mButton;
    private Button mButtonSelect;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mMainView = inflater.inflate(R.layout.main, container, false);
        mButton       = mMainView.findViewById(R.id.buttonEnable);
        mButtonSelect = mMainView.findViewById(R.id.buttonSelect);
        return mMainView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        text = Objects.requireNonNull(getView()).findViewById(R.id.editText);

        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                switch (charSequence.toString()) {
                    case "adelin":
                        showLongSnack("404 cromozon not found");
                        break;
                    case "lupu":
                        showLongSnack("404 artera not found");
                        break;
                    case "draica":
                        showLongSnack("al mai jmek xd");
                        break;
                    case "sunt smecher":
                        showLongSnack("te cheama draica?");
                        break;
                    case "vam adus testele":
                        showLongSnack("at loat doi");
                        break;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void showLongSnack(String textToShow){
        Snackbar.make(getView(), textToShow, Snackbar.LENGTH_LONG).show();
    }

    public Button getSelectButton(){
        return mButtonSelect;
    }

    public Button getEnableButton(){
        return mButton;
    }


}