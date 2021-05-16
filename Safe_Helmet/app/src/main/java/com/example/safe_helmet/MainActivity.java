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
    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;

    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;
    //블루투스 api
    BluetoothHeadset bluetoothHeadset;
    // Get the default adapter
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Button battery_btn;

    Toolbar myToolbar;                          //툴바 선언
    public static Context mContext;             //MainActivity를 가르키는 context

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };

    String[] permission_list = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
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
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetoothOn();
//        list
        bluetooth_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
//                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//                registerReceiver(receiver, filter);
                Toast.makeText(getApplicationContext(), "이미기기와 연결됨", Toast.LENGTH_LONG).show();
                bluetoothOn();
                listPairedDevices();

            }
        });
        //bluetoothOff();



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



        mBluetoothHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == BT_MESSAGE_READ){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    //mTvReceiveData.setText(readMessage);
                }
            }
        };
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
            Toast.makeText(getApplicationContext(), phone_num+"\n"+sms, Toast.LENGTH_LONG).show();              //test
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
        mContext = this;

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


    void bluetoothOn() {
        if(mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
        }
        else {
            if (mBluetoothAdapter.isEnabled()) {
                //Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어 있습니다.", Toast.LENGTH_LONG).show();
                //mTvBluetoothStatus.setText("활성화");
                listPairedDevices();
            }
            else {
                //Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_LONG).show();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
                listPairedDevices();
            }
        }
    }
    void bluetoothOff() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되었습니다.", Toast.LENGTH_SHORT).show();
            //mTvBluetoothStatus.setText("비활성화");
        }
        else {
            Toast.makeText(getApplicationContext(), "블루투스가 이미 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BT_REQUEST_ENABLE:
                if (resultCode == RESULT_OK) { // 블루투스 활성화를 확인을 클릭하였다면
                    Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_LONG).show();
                    //mTvBluetoothStatus.setText("활성화");
                } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화를 취소를 클릭하였다면
                    Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_LONG).show();
                    //mTvBluetoothStatus.setText("비활성화");
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    void listPairedDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            mPairedDevices = mBluetoothAdapter.getBondedDevices();

            if (mPairedDevices.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("장치 선택");
                mListPairedDevices = new ArrayList<String>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
                    //mListPairedDevices.add(device.getName() + "\n" + device.getAddress());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        connectSelectedDevice(items[item].toString());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
                Intent discoverableIntent =
                        new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    void connectSelectedDevice(String selectedDeviceName) {
        for(BluetoothDevice tempDevice : mPairedDevices) {
            if (selectedDeviceName.equals(tempDevice.getName())) {
                mBluetoothDevice = tempDevice;
                Toast.makeText(getApplicationContext(), "기기와 연결되었습니다", Toast.LENGTH_LONG).show();
                break;
            }
        }
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
    }

    class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }







        protected void onDestroy() {

            // Don't forget to unregister the ACTION_FOUND receiver.
            unregisterReceiver(receiver);
        }

        public void write(String str) {
            byte[] bytes = str.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "데이터 전송 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 해제 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
