package com.vlath.keyboard.setup;

/* This kinda sucks tho*/
//Draica agrees

import android.content.Intent;
import android.graphics.Color;
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

    @ViewById(R.id.toolbar_top)
    Toolbar mBar;

    @ViewById(R.id.bttm_nav)
    BottomNavigationView mNavigationView;

    @FragmentById(R.id.nav_host_fragment)
    NavHostFragment mNavHost;

    @AfterViews
    void viewsInit(){
        mBar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mBar);
        setTitle("BeHe Keyboard");
        NavigationUI.setupWithNavController(mNavigationView, mNavHost.getNavController());
    }

    public void select(View v){
        InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
        if (imeManager != null) {
            imeManager.showInputMethodPicker();
        } else {
            Toast.makeText(this, "Not possible" , Toast.LENGTH_LONG).show();
        }
    }

    public void enable(View v){
        this.startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
    }

}