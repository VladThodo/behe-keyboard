/*
 * Copyright (C) 2020 Vlad Todosin
 *
 * Licensed under the Apache License 2.0
 * */

package com.vlath.keyboard.setup;

/* This kinda sucks tho*/
//Draica agrees

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import com.vlath.keyboard.R;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.ViewById;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

@EActivity(R.layout.activate)
public class Main extends AppCompatActivity {

    @ViewById(R.id.bttm_nav)
    BottomNavigationView mNavigationView;

    @FragmentById(R.id.nav_host_fragment)
    NavHostFragment mNavHost;

    @AfterViews
    void viewsInit(){
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
        NavigationUI.setupWithNavController(mNavigationView, mNavHost.getNavController());
    }

    public void select(View v){

    }

    public void enable(View v){
        this.startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
    }

}