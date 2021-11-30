package com.save_us.safe_helmet;

import android.Manifest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.location.Location;
import android.widget.ToggleButton;

import com.example.safe_helmet.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Looper;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity<ViewLatitude> extends AppCompatActivity {
//    public static GPS_to_Speed gps;             // GPSTracker class
    private static TextView ViewLatitude;
    private static TextView ViewLongitude;
    private static TextView ViewDistance;
    private BackPressCloseHandler BackPressCloseH;
    static String[] phone_num_list;
    static String message_text;
    static boolean BT_DATA = false;
    static String GPS;
    TextView ViewGPS;
    ToggleButton ToggleBtnGPS;
    Button bluetooth_btn;
    Button battery_btn;
    //Button sensor_btn;


    Toolbar myToolbar;                          //툴바 선언
    public static Context mContext;             //MainActivity를 가르키는 context

    String[] permission_list = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    boolean bluetooth = true;                   //충격센서의 충격 여부
    int battery_percent = -1;


    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;


    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소
    Location mCurrentLocatiion;
    LatLng currentPosition;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;
    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.
    // (참고로 Toast에서는 Context가 필요했습니다.)

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BackPressCloseH = new BackPressCloseHandler(this);
        /*   앱 실행시 Background Service 실행   */
        Intent serviceintent = new Intent(this, MyService.class);
        startService(serviceintent);

        checkPermission();
        /*   Toolbar Settings   */
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bluetooth_btn = (Button) findViewById(R.id.bluetooth);
        //sensor_btn = (Button)findViewById(R.id.sensor);
        mContext = this;

        bluetooth_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,
                        ConnectBluetoothActivity.class);
                startActivity(intent);
            }
        });




        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mLayout = findViewById(R.id.layout_main);


        /*  msg  */
        Settings_Data_Load(1);
        Toast.makeText(getApplicationContext(), "전송 완료 되었습니다", Toast.LENGTH_LONG).show();
        Toast.makeText(this, message_text, Toast.LENGTH_LONG).show();


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

    /*   Message Information   */
    public static void Settings_Data_Load(int DANGER){
        /*
         * 하나의 메세지당 70자로 제한됨
         */
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext); // settring data load
        String ph_num = sharedPref.getString("phone_number", "");
        phone_num_list = ph_num.split("\n");                                             // phone_nunber load

        String message_user = sharedPref.getString("message", ".");               // message load
        String User_Name = sharedPref.getString("name", "");                     // name load

        String[] message_split = message_user.split("\n");

        /*   default message && end message   */
        String[] message_plain = {"[App발신]\n'Safe_Helmet'에서 알려드립니다.\n",
                "From. "+User_Name
        };

        StringBuffer add = new StringBuffer(message_user);   //문자열 수정을 위해 선언

        int length=message_plain[0].length();
        for(int i=0; i<message_split.length; i++)
            length += message_split[i].length();
        add.insert(0, message_plain[0]);
        if (DANGER == 1){       //경상사고 우려
            add.insert(length, "\n30km/h이상의 속도로 충돌하였습니다.\n경");
        }
        else if (DANGER == 2){  //중상사고 우려
            add.insert(length, "60km/h이상의 속도로 충돌하여 중상 우려의 사고입니다.");
        }
        else if (DANGER == 3){  //사망사고 우려
            add.insert(length, "100km/h이상의 속도로 충돌하여 사망 우려의 사고입니다.");
        }

//        message_text = add.toString();
        message_text = message_plain[0]+message_user;
        MainActivity.Message_Send();    //메세지 보내기_1


        message_text = "";
        if (DANGER == 1){       //경상사고 우려
            message_text = message_text + "\n30km/h이상의 속도로 충돌하였습니다.\n경상 우려의 ";
        }
        else if (DANGER == 2){  //중상사고 우려
            message_text = message_text + "\n60km/h이상의 속도로 충돌하였습니다.\n중상 우려의 ";
        }
        else if (DANGER == 3){  //사망사고 우려
            message_text = message_text + "\n100km/h이상의 속도로 충돌하였습니다.\n사망 우려의 ";
        }
        message_text = message_text +"추락 사고입니다.\n"+ message_plain[1];
//
//        StringBuffer add_2 = new StringBuffer(message_tmp);
//        String[] add_text_2 = new String[0];
//        length=add_text_2[0].length();
//        //add.insert(length, GPS);  //주소 정보
//
//
//
//
//
//
//        add.insert(length, add_text[1]);
//
//        message_text = add.toString();
        MainActivity.Message_Send();    //메세지 보내기_2
    }

    public static void Message_Send(){
        for(int i=0; i<MainActivity.phone_num_list.length; i++){
            String sms = MainActivity.message_text.toString();
            try {
                //전송
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(MainActivity.phone_num_list[i], null, sms, null, null);
                Log.d("test", "전송 완료 되었습니다");
            } catch (Exception e) {
                Log.d("test", e.toString());
                e.printStackTrace();
            }
        }

    }

    public void onBackPressed(){
        BackPressCloseH.onBackPressed();
    }

}
