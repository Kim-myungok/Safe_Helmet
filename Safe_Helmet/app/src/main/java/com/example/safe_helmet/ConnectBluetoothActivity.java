package com.example.safe_helmet;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ConnectBluetoothActivity extends AppCompatActivity {
    // onActivityResult의 RequestCode 식별자
    private static final int REQUEST_NEW_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // 블루투스 사용 객체
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private Set<BluetoothDevice> bluetoothDeviceSet;
    private BluetoothSocket bluetoothSocket;
    public static InputStream inputStream;

    // xml 객체
    private ListView listView_pairing_devices; // 페어링 된 디바이스 리스트뷰 객체

    // 일반 객체
    private String device_name;

    Toolbar myToolbar;                          //툴바 선언
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_bluetooth);

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 리스트 뷰 객체와 xml id 연결
        listView_pairing_devices = (ListView)findViewById(R.id.listview_pairing_devices);

        // 버튼 객체 생성 및 xml id 연결
        Button button_setting_menu = (Button)findViewById(R.id.button_setting_menu);
        // "디바이스 신규 등록" 버튼 클릭 이벤트
        button_setting_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 블루투스 설정 화면 띄워주기
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                // 종료 후 onActivityResult 호출, requestCode가 넘겨짐
                startActivityForResult(intent, REQUEST_NEW_DEVICE);
            }
        });

        // 블루투스 활성화 확인 함수 호출
        checkBluetooth();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            // 블루투스 설정화면 종료 시
            case REQUEST_NEW_DEVICE :
                checkBluetooth();
                break;
            // 블루투스 활성화 선택 종료 시
            case REQUEST_ENABLE_BT :
                // 활성화 버튼을 눌렀을 때
                if(resultCode == RESULT_OK) {
                    selectDevice();
                }   // 취소 버튼을 눌렀을 때
                else if(resultCode == RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "블루투스를 활성화 하지 않아 앱을 종료합니다.", Toast.LENGTH_LONG).show();
                    finish();// 앱 종료
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 블루투스 활성화 확인 함수
    public void checkBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 단말기가 블루투스를 지원하지 않을 때
        if(bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "단말기가 블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show();
            finish(); // 앱 종료
        }
        // 블루투스를 지원할 때
        else {
            // 블루투스가 활성화 상태
            if(bluetoothAdapter.isEnabled()) {
                // 블루투스 선택 함수 호출
                selectDevice();
            }
            // 블루투스가 비 활성화 상태
            else {
                // 블루투스를 활성화
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_ENABLE_BT);
            }
        }
    }

    // 블루투스 선택 함수
    public void selectDevice() {
        // 페어링 된 디바이스 목록을 불러옴
        bluetoothDeviceSet = bluetoothAdapter.getBondedDevices();

        // 리스트를 만듬
        List<String> list = new ArrayList<>();
        for(BluetoothDevice bluetoothDevice : bluetoothDeviceSet) {
            list.add(bluetoothDevice.getName());
        }

        // 어레이어뎁터로 리스트뷰에 리스트를 생성
        final ArrayAdapter arrayAdapter = new ArrayAdapter(ConnectBluetoothActivity.this, android.R.layout.simple_list_item_1, list);
        listView_pairing_devices.setAdapter(arrayAdapter);
        listView_pairing_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // 리스트 뷰의 아이템을 클릭했을 때
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                device_name = arrayAdapter.getItem(position).toString();

                // 프로그레스 다이얼로그 생성
                CheckTypesTask task = new CheckTypesTask();
                task.execute();
            }
        });
    }

    // 클릭 된 디바이스와 연결하는 함수
    public void connectDevice(String deviceName) {
        // 블루투스 연결 할 디바이스를 찾음
        for(BluetoothDevice device : bluetoothDeviceSet) {
            if(deviceName.equals(device.getName())) {
                bluetoothDevice = device;
                break;
            }
        }

        // UUID 생성
        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        try {
            // 블루투스 소켓 생성
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            // 데이터 받기 위해 인풋스트림 생성
            inputStream = bluetoothSocket.getInputStream();

            // 블루투스 수신 시작 호출
            Intent intent = new Intent(ConnectBluetoothActivity.this, BluetoothCommunication.class);
            startActivity(intent);
        }
        catch (Exception e) {
            // 쓰레드에서 UI처리를 위한 핸들러
            Message msg = handler.obtainMessage();
            handler.sendMessage(msg);
        }
    }

    // 핸들러 선언
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(), "블루투스 연결을 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
        }
    };

    // 프로그레스 다이얼로그 생성
    private class CheckTypesTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog asyncDialog = new ProgressDialog(
                ConnectBluetoothActivity.this);

        @Override
        protected void onPreExecute() {
            asyncDialog.setCancelable(false);
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage(device_name + "과 연결중입니다.");

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        // 백그라운드에서 실행
        @Override
        protected Void doInBackground(Void... arg0) {
            connectDevice(device_name);
            return null;
        }

        // 백그라운드가 모두 끝난 후 실행
        @Override
        protected void onPostExecute(Void result) {
            asyncDialog.dismiss();
            super.onPostExecute(result);
        }
    }
}