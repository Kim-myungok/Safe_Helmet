package com.save_us.safe_helmet;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.safe_helmet.R;

import java.util.Timer;
import java.util.TimerTask;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class SensorActivity extends AppCompatActivity {
    public static int read;
    int bluetooth_data;
    private static SensorManager sensorManager;
    private static BluetoothSPP bt;
    //Using the Accelometer
    SensorEventListener AccLis;
    Sensor AccelometerSensor = null;

    //Using the Vibration_sensor
    Sensor vibration_sensor = null;
    private double Sensorvalue;
    Context sContext;
    Button btnSend;
    TextView temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        bt = new BluetoothSPP(this); //Initializing
        //Using the Gyroscope & Accelometer
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        vibration_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ALL);
        sContext = this;
        temp = findViewById(R.id.editText);

        if (!bt.isBluetoothAvailable()) {//블루투스 사용 불가
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }


        TimerTask tt = new TimerTask() {
            //TimerTask 추상클래스를 선언하자마자 run()을 강제로 정의하도록 한다.
            @Override
            public void run() {
                /////////////////// 추가한 코드 ////////////////////
                //Toast.makeText(SensorActivity.this, Integer.toString(read), Toast.LENGTH_SHORT).show();
                //String[] array = message.split(",");
                //text_edit();
                //////////////////////////////////////////////////
            }
        };
        Toast.makeText(SensorActivity.this, Integer.toString(read), Toast.LENGTH_SHORT).show();

        /////////// / Timer 생성 //////////////
        Timer timer = new Timer();
        timer.schedule(tt, 0, 1000);
        //////////////////////////////////////



////
    }
    void text_edit(){
        temp.setText(Integer.toString(read));
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        SensorManager.unregisterListener(this);
//    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//        SensorManager.registerListener(this, vibration_sensor,
//                SensorManager.SENSOR_DELAY_UI);
//    }
}
//    private class AccelometerListener implements SensorEventListener {
//
//        @Override
//        public void onSensorChanged(SensorEvent event) {
//
//            double accX = event.values[0];
//            double accY = event.values[1];
//            double accZ = event.values[2];
//
//            double angleXZ = Math.atan2(accX,  accZ) * 180/Math.PI;
//            double angleYZ = Math.atan2(accY,  accZ) * 180/Math.PI;
//
//            Log.e("LOG", "ACCELOMETER           [X]:" + String.format("%.4f", event.values[0])
//                    + "           [Y]:" + String.format("%.4f", event.values[1])
//                    + "           [Z]:" + String.format("%.4f", event.values[2])
//                    + "           [angleXZ]: " + String.format("%.4f", angleXZ)
//                    + "           [angleYZ]: " + String.format("%.4f", angleYZ));
//
//        }
//
//        @Override
//        public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//        }