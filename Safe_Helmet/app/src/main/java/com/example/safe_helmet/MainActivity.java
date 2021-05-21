package com.example.safe_helmet;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    Button bluetooth_btn;
    Button battery_btn;

    Toolbar myToolbar;                          //툴바 선언
    public static Context mContext;             //MainActivity를 가르키는 context

    String[] permission_list = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    String[] phone_num_list;
    String message_text;

    boolean bluetooth = true;                   //충격센서의 충격 여부
    int battery_percent = -1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bluetooth_btn = (Button)findViewById(R.id.bluetooth);
        mContext = this;

        bluetooth_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ConnectBluetoothActivity.class);
                startActivity(intent);

            }
        });


        //충격센서의 충격에 따라 Call_SOS() 실행
        if (!bluetooth) {
            Settings_Data_Load();
            Call_SOS();
            for(int i=0; i<phone_num_list.length; i++){
                Message_Send(phone_num_list[i]);
            }
        }
//        else{
//            bluetooth_listener = new View.OnClickListener(){
//                @Override
//                public void onClick(View v) {
//                    bluetooth_btn.setText(String.valueOf("블루투스 페어링\nNolinked"));
//                }
//            };
//        }
//        if (battery_percent < 0) {
//            battery_btn.setText(String.valueOf("배터리\n"+battery_percent+"%"));
//        }
    }


    /*   toolbar_setting   */
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.setting_btn:  //설정화면으로 이동
                Intent intent_setting = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(intent_setting, 101);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*   get_access   */
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
                    Toast.makeText(getApplicationContext(), "앱권한설정하세요", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /*   Make call   */
    public void Call_SOS() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                Toast toast = Toast.makeText(MainActivity.this, "권한 없음", Toast.LENGTH_SHORT);
                toast.show();
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


    /*   Massage Send   */
    public void Message_Send(String phone_num) {
        String sms = message_text.toString();
        try {
            //전송
            //Toast.makeText(getApplicationContext(), phone_num+"\n"+sms, Toast.LENGTH_LONG).show();              //test
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone_num, null, sms, null, null);
            //Toast.makeText(getApplicationContext(), Arrays.toString(message_text), Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(), "전송 완료 되었습니다", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            //Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void Settings_Data_Load(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext); // settring data load
        String ph_num = sharedPref.getString("phone_number", "");
        phone_num_list = ph_num.split("\n");                                             // phone_nunber load

        String message_tmp = sharedPref.getString("message", ".");               // message load
        String User_Name = sharedPref.getString("name", "");                     // name load

        String[] message_split = message_tmp.split("\n");

        /*   default message && end message   */
        String[] add_text = {"[App발신]\n'Safe_Helmet'에서 알려드립니다.\n",
                "\n\nFrom."+User_Name
        };

        StringBuffer add = new StringBuffer(message_tmp);   //문자열 수정을 위해 선언

        int length=add_text[0].length();
        for(int i=0; i<message_split.length; i++)
            length += message_split[i].length();
        add.insert(0, add_text[0]);
        add.insert(length, add_text[1]);


        message_text = add.toString();
    }


}
