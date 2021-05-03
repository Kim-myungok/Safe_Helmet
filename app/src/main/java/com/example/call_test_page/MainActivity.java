package com.example.call_test_page;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


public class MainActivity<CALL_PHONE> extends AppCompatActivity {

    private Button buttonSend;
    private EditText textPhoneNo;
    private EditText textSMS;
    private static final int MY_PERMISSION_STORAGE = 1111;
    String[] permission_list = {
            Manifest.permission.CALL_PHONE
//            Manifest.permission.SEND_SMS
    };
    //충격센서의 충격 여부
    boolean bluetooth = true;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        checkPermission();

        ContextCompat.checkSelfPermission(this,Manifest.permission.CALL_PHONE);
        //충격센서의 충격에 따라 call_sos() 실행
        if(bluetooth) call_sos();
    }


    public void checkPermission() {
        //현재 안드로이드 버전이 6.0미만이면 메서드를 종료한다.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        for (String permission : permission_list) {
            //권한 허용 여부를 확인한다.
            int chk = checkCallingOrSelfPermission(permission);
            if (chk == PackageManager.PERMISSION_DENIED) {
                //권한 허용을여부를 확인하는 창을 띄운다
                requestPermissions(permission_list, 0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            for (int i = 0; i < grantResults.length; i++) {
                //허용됬다면
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(getApplicationContext(), "앱 권한 설정하세요", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void call_sos() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        Toast.makeText(getApplicationContext(), "call_sos();", Toast.LENGTH_LONG).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                // 권한 없음
            } else {
                Intent call = new Intent(Intent.ACTION_CALL, Uri.parse("tel:01072904078"));
                startActivity(call);
                // 권한 있음
            }
        }
        // OS가 Marshmallow 이전일 경우 권한체크를 하지 않는다.
        else {

        }
    }

    //전화걸기 버튼 누름시, 통화걸기
    public void call_Button(View v) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        Toast.makeText(getApplicationContext(), "call_Button();", Toast.LENGTH_LONG).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                checkPermission();
                // 권한 없음
            } else {
                Intent call = new Intent(Intent.ACTION_CALL, Uri.parse("tel:01072904078"));
                startActivity(call);
                // 권한 있음
            }
        }
        // OS가 Marshmallow 이전일 경우 권한체크를 하지 않는다.
        else {

        }
    }
}