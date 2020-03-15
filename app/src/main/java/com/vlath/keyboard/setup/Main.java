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

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
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
        Log.d("Name", getResources().getResourceName(2132082703));
        setTheme(R.style.AppTheme);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
        NavigationUI.setupWithNavController(mNavigationView, mNavHost.getNavController());
    }

    public void select(View v){

    }

    public void enable(View v){
        this.startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
    }

}