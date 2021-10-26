package com.save_us.safe_helmet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.safe_helmet.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    //Toolbar myToolbar;                          //툴바 선언

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);


        //myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        //setSupportActionBar(myToolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
    }
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }

    /*   toolbar_setting   */
//    public boolean onCreateOptionsMenu(Menu menu) {
//        //return super.onCreateOptionsMenu(menu);
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.menu_blue, menu);
//        return true;
//    }
//
//    public boolean onOptionsItemSelected(MenuItem item) {
//        //return super.onOptionsItemSelected(item);
//        switch (item.getItemId()) {
//            case R.id.setting_btn:  //설정화면으로 이동
//                Intent intent_setting = new Intent(getApplicationContext(), SettingsActivity.class);
//                startActivityForResult(intent_setting, 101);
//                return true;
//            case R.id.home_btn:  //홈화면으로 이동
//                Intent intent_home = new Intent(getApplicationContext(), MainActivity.class);
//                startActivityForResult(intent_home, 101);
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

}