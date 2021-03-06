package com.save_us.safe_helmet;

import android.Manifest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.os.Build;
import android.os.Bundle;

import android.location.Address;
import android.location.Geocoder;

import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.example.safe_helmet.R;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity<ViewLatitude> extends AppCompatActivity {
    public static GpsService gps;             // GPSTracker class
    public static String Address;
    private BackPressCloseHandler BackPressCloseH;
    static String[] phone_num_list;
    static String message_text;
    static boolean BT_DATA = false;
    Button bluetooth_btn;
    Button setting_btn;

    Toolbar myToolbar;                          //툴바 선언
    public static Context mContext;             //MainActivity를 가르키는 context

    String[] permission_list = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초
    private LocationRequest locationRequest;

    private View mLayout;  // Snackbar 사용하기 위해 View 필요

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.layout_main);

        /*   뒤로가기 핸들러 실행   */
        BackPressCloseH = new BackPressCloseHandler(this);

        /*   앱화면 꺼짐방지   */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*   퍼미션 체크   */
        checkPermission();
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        /*   앱 실행시 Background Service 실행   */
        Intent MyService_intent = new Intent(getApplicationContext(),MyService.class);
        startService(MyService_intent); // 서비스 시작

        /*   툴바 설정   */
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        mContext = this;

        /*   버튼 설정   */
        bluetooth_btn = (Button) findViewById(R.id.bluetooth);
        bluetooth_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,
                        ConnectBluetoothActivity.class);
                startActivity(intent);
            }
        });
        setting_btn = (Button)findViewById(R.id.setting);
        setting_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*   message test   */
//                Settings_Data_Load(TEST_DANGER);

                Intent intent_setting = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(intent_setting, 101);
            }
        });
    }

    /*   GPS_Info_Reading   */
    public static void GPS(){
        /*   위치 관련   */
        gps = new GpsService(MainActivity.mContext);

        if (gps.isGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            getCurrentAddress(latitude, longitude);

        } else
            gps.showSettingAlert();
    }

    /*   Geocoder로 위도경도를 주소값으로 변환   */
    public static void getCurrentAddress( double latitude, double longitude) {
        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(MainActivity.mContext, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Log.d("test", "GoCoder 서비스 사용불가");
            return;
        } catch (IllegalArgumentException illegalArgumentException) {
            Log.d("test", "잘못된 GPS 좌표");
            return;
        }
        if (addresses == null || addresses.size() == 0) {
            Log.d("test", "주소 미발견");
        }
        Address address = addresses.get(0);
        /*   시간 나타내기   */
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat DF = new SimpleDateFormat("yy년 MM월 dd일 hh:mm:ss");
        Address = address.getAddressLine(0).toString();
        Log.d("test", DF.format(date)+"    ||   현재주소: "+Address);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("test", "onStart");
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
                Intent serviceintent = new Intent(MainActivity.this, MyService.class);
                stopService(serviceintent);
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
    public static int Settings_Data_Load(int DANGER){
        /*
         * 하나의 메세지당 70자로 제한됨
         */

        /*   PART_1_message loading && parsing   */
        GPS();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext); // settring data load
        String ph_num = sharedPref.getString("phone_number", "");
        phone_num_list = ph_num.split("\n");                                             // phone_nunber load

        String message_user = sharedPref.getString("message", ".");               // message load
        String User_Name = sharedPref.getString("name", "");                      // name load
        String[] message_split = message_user.split("\n");

        /*   PART_2_start message && end message   */
        String[] message_plain = {"[App발신]\n'Safe_Helmet'에서 알려드립니다.\n",
                "From. "+User_Name
        };
        /*   PART_3_message sending   */
        message_text = message_plain[0]+message_user;
        MainActivity.Message_Send();    //메세지 보내기_1



//        int DANGER_len_to_10pow = (int)Math.pow(10,Integer.toString(DANGER).length()-1); //3
        if (DANGER % 3 == 2){       //사망 우려
            message_text = "사망 우려 ";
            if(DANGER / 3 == 1) message_text = message_text +"추락 사고입니다.";
            else if(DANGER / 3 == 2) message_text = message_text +"충돌 사고입니다.";
        } else if (DANGER % 3 == 1){       //중상 우려
            message_text = "중상 우려 ";
            if(DANGER / 3 == 1) message_text = message_text +"추락 사고입니다.";
            else if(DANGER / 3 == 2) message_text = message_text +"충돌 사고입니다.";
        } else if (DANGER % 3 == 0){       //사망 우려
            message_text = "경상 우려 ";
            if(DANGER / 3 == 1) message_text = message_text +"추락 사고입니다.";
            else if(DANGER / 3 == 2) message_text = message_text +"충돌 사고입니다.";
        }
        MainActivity.Message_Send();    //메세지 보내기_2


        message_text = "현주소 : "+Address+ "\n" + message_plain[1];
        MainActivity.Message_Send();    //메세지 보내기_3
        Toast.makeText(MainActivity.mContext, "전송 완료 되었습니다", Toast.LENGTH_LONG).show();

        return 0;
    }

    /*   Message Sending   */
    public static void Message_Send(){
        for(int i=0; i<MainActivity.phone_num_list.length; i++){
            String sms = MainActivity.message_text.toString();
            try {
                //전송
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(MainActivity.phone_num_list[i], null, sms, null, null);

            } catch (Exception e) {
                Toast.makeText(MainActivity.mContext, e.toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    /*   두 번 뒤로가기 했을때 종료   */
    public void onBackPressed(){
        BackPressCloseH.onBackPressed();
    }
}
