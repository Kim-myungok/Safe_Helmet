package com.save_us.safe_helmet;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.safe_helmet.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

public class BluetoothCommunication extends AppCompatActivity {

    // xml 객체 선언
    private LinearLayout linearLayout_setcolor;
    private TextView textView_connection_status;
    private TextView textView_connection_explaination;
    private ListView listView_alarm_log;
    private Button button_pairing;
    private TextView textView_alarm_log;
    private BluetoothSPP bt;

    // 일반 변수 객체 선언
    private List<String> list;
    private ArrayAdapter arrayAdapter;
    private int log_num = 1;

    // 쓰레드 사용 객체 선언
    private int readBufferPosition;
    private byte[] readBuffer;
    private Thread thread;

    // WakeLock 사용 객체
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    Toolbar myToolbar;                          //툴바 선언
    public int byteAvailabe;
    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_communication);

        bt = new BluetoothSPP(this); //Initializing
        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // xml 객체 id 연결
        linearLayout_setcolor = (LinearLayout)findViewById(R.id.linearlayout_setcolor);
        textView_connection_status = (TextView)findViewById(R.id.textview_connection_status);
        textView_connection_explaination = (TextView)findViewById(R.id.textview_connection_explaination);
        listView_alarm_log = (ListView)findViewById(R.id.listview_alarm_log);
        button_pairing = (Button)findViewById(R.id.button_pairing);
        textView_alarm_log = (TextView)findViewById(R.id.textview_alarm_log);

        // 리스트 뷰 어댑터 생성
        list = new ArrayList<>();
        arrayAdapter = new ArrayAdapter(BluetoothCommunication.this, android.R.layout.simple_list_item_1, list);
        // 항상 최하단 아이템으로 유지
        listView_alarm_log.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView_alarm_log.setAdapter(arrayAdapter);

        // 페어링 하기 버튼 클릭 이벤트
        button_pairing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(BluetoothCommunication.this, MainActivity.class));
                Toast.makeText(getApplicationContext(), "블루투스 기기와 연결되었습니다.", Toast.LENGTH_LONG).show();
            }
        });

        // WakeLock 객체 생성 및 설정
        powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "WAKELOCK");

        // 동적 버튼 객체 생성
        final Button newbtn = new Button(BluetoothCommunication.this);

        // UI 변경은 핸들러에서 처리
        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            public void handleMessage(Message msg) {

            }
        };



        // 수신 버퍼 저장 위치
        readBufferPosition = 0;
        readBuffer = new byte[10];

        // 문자 수신 쓰레드
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 인터럽트 호출 전까지 반복
                while(!Thread.currentThread().isInterrupted()) {
                    // 수신 데이터 확인 변수
                    byteAvailabe = 0;

                    // 문자열 개수를 받아옴
                    try {
                        byteAvailabe = ConnectBluetoothActivity.inputStream.available();
                        String BTdata = Integer.toString(byteAvailabe);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }

                    Log.d("Thread", "From Bluetooth Data : " + byteAvailabe);

                    // 데이터가 수신된 경우
                    if(byteAvailabe > 0) {
                        // 데이터 크기만큼 바이트 배열 생성
                        byte[] packetByte = new byte[byteAvailabe];
                        // 바이트 배열 크기만큼 읽어옴
                        try {
                            ConnectBluetoothActivity.inputStream.read(packetByte);

                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }

                        for(int i = 0; i < byteAvailabe; i++) {
                            final byte readData = packetByte[i];
                            if(readData != '\n') {
                                // 읽어온 바이트 배열을 인코딩 된 배열로 복사
                                byte[] encodedByte = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedByte, 0, encodedByte.length);

                                try {
                                    String data = new String(encodedByte, "US-ASCII");
                                }
                                catch (UnsupportedEncodingException e) {
                                    e.getStackTrace();
                                }

                                readBufferPosition = 0;

                                final PendingIntent pendingIntent = PendingIntent.getActivity(BluetoothCommunication.this, 0,
                                        new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

                                handler.post(new Runnable() {
                                    // 알림 객체 선언
                                    NotificationManager notificationManager;
                                    Notification.Builder builder;

                                    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
                                    @Override
                                    public void run() {
                                        // 버튼 숨기기
                                        button_pairing.setVisibility(View.INVISIBLE);
                                        listView_alarm_log.setVisibility(View.VISIBLE);
                                        textView_alarm_log.setVisibility(View.VISIBLE);

                                        // 0 입력 받을 때
                                        if(readData == 48) {
                                            linearLayout_setcolor.setBackgroundColor(Color.rgb(185,255,198));
                                            textView_connection_status.setTextColor(Color.BLACK);
                                            textView_connection_status.setText("블루투스 연결 상태 : 정상");
                                            textView_connection_explaination.setText("블루투스 기기와 연결되었습니다.");
                                            startActivity(new Intent(BluetoothCommunication.this, MainActivity.class));
                                        }
                                        // 1 입력 받을 때
                                        else {

                                            String nowDate = "센서값 : "+byteAvailabe;

                                                    linearLayout_setcolor.setBackgroundColor(Color.rgb(243,243,255));
                                            textView_connection_explaination.setText(Integer.toString(byteAvailabe));
                                            MainActivity.BT_DATA = true;
                                            if (MainActivity.BT_DATA) {
                                                MainActivity.BT_DATA = false;
                                                MainActivity.Settings_Data_Load();
                                                int permissionCheck = ContextCompat.checkSelfPermission(BluetoothCommunication.this, Manifest.permission.CALL_PHONE);
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                                                        Toast toast = Toast.makeText(BluetoothCommunication.this, "권한 없음", Toast.LENGTH_SHORT);
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

                                                for(int i=0; i<MainActivity.phone_num_list.length; i++){
                                                    String sms = MainActivity.message_text.toString();
                                                    try {
                                                        //전송
                                                        SmsManager smsManager = SmsManager.getDefault();
                                                        smsManager.sendTextMessage(MainActivity.phone_num_list[i], null, sms, null, null);
                                                        Toast.makeText(getApplicationContext(), "전송 완료 되었습니다", Toast.LENGTH_LONG).show();
                                                    } catch (Exception e) {
                                                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                                                        e.printStackTrace();
                                                    }
                                                }
                                                try {
                                                    Thread.sleep( 1000 );	//5초씩 쉰다.
                                                } catch (Exception e) {
                                                }
                                            }
                                            MainActivity.BT_DATA = false;
                                            // 알림 객체 설정
                                            builder = new Notification.Builder(BluetoothCommunication.this)
                                                    .setSmallIcon(R.drawable.ic_launcher_background) // 아이콘 설정
                                                    .setContentTitle(nowDate) // 제목 설정
                                                    .setContentText("센서가 감지되었습니다.") // 내용 설정
                                                    .setAutoCancel(true)
                                                    .setTicker("센서가 감지되었습니다.") // 한줄 내용 설명
                                                    .setContentIntent(pendingIntent);

                                            notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                                            // 젤리빈 버전 이상 알림
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                                notificationManager.notify(0, builder.build());
                                            }

                                            // 로그 리스트에 추가
                                            list.add(log_num + ". " + nowDate);
                                            arrayAdapter.notifyDataSetChanged();
                                            log_num++;
                                        }
                                    }
                                });
                            }
                        }
                    }
                    // 데이터가 수신되지 않은 경우
                    else {
                        // 메세지 핸들러 호출
                        Message msg = handler.obtainMessage();
                        handler.sendMessage(msg);
                    }

                    try {
                        // 2초 간격으로 반복
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        // 쓰레드 시작
        thread.start();
    }


    /*   toolbar_setting   */
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_blue, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.setting_btn:  //설정화면으로 이동
                Intent intent_setting = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(intent_setting, 101);
                return true;
            case R.id.home_btn:  //홈화면으로 이동
                Intent intent_home = new Intent(getApplicationContext(), MainActivity.class);
                startActivityForResult(intent_home, 101);
                intent_home.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}