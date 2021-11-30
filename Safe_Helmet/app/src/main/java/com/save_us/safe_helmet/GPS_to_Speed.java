//package com.save_us.safe_helmet;
//
//import android.app.AlertDialog;
//import android.app.Service;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.location.Criteria;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.provider.Settings;
//import android.util.Log;
//
///**
// * Created by yong on 15. 2. 12.
// */
//public class GPS_to_Speed extends Service implements LocationListener {
//    private final Context mContext;
//
//    // GPS 사용여부
//    boolean isGPSEnabled = false;
//
//    // 네트워크 사용여부
//    boolean isNetWorkEnabled = false;
//
//    // GPS 상태값
//    boolean isGetLocation = false;
//
//
//    Location location;
//    double lat; // 위도
//    double lon; // 경도
//
//    private long startTime = -1;
//    private Location beforeLocation;
//    private Location curLocation;
//
//    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 1;
//
//    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
//
//    protected LocationManager locationManager;
//
//    public GPS_to_Speed(Context context) {
//        this.mContext = context;
//        getLocation();
//    }
//
//    public Location getLocation() {
//        try {
//            Criteria criteria = new Criteria();
//
//            criteria.setAccuracy(Criteria.ACCURACY_FINE);     // 정확도
//            criteria.setPowerRequirement(Criteria.POWER_LOW); // 전원소비량
//            criteria.setAltitudeRequired(true);              // 고도
//            criteria.setBearingRequired(false);              // 기본 정보, 방향, 방위
//            criteria.setSpeedRequired(false);                // 속도
//            criteria.setCostAllowed(true);                   // 위치정보 비용
//
//            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
//            String provider = locationManager.getBestProvider(criteria, true);
//
//            // GPS 정보 가져오기
//            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//
//            // 현재 네트워크 상태 값 알아오기
//            isNetWorkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//            if (!isGPSEnabled && !isNetWorkEnabled) {
//
//            } else {
//                try {
//                    if (isNetWorkEnabled) {
//                        locationManager.requestLocationUpdates(provider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
//                        if (locationManager != null) {
//                            location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
//                            if (location != null) {
//                                // 위도 경도 저장
//                                lat = location.getLatitude();
//                                lat = location.getLongitude();
//                            }
//                        }
//                    }
//                } catch (SecurityException e){
//                    e.printStackTrace();
//                }
//                this.isGetLocation = true;
//                if (isGPSEnabled) {
//                    try {
//                        if (location == null) {                  // LocationManager.GPS_PROVIDER
//                            locationManager.requestLocationUpdates(provider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATE, this);
//                            if (locationManager != null) {
//                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                                if (location != null) {
//                                    lat = location.getLatitude();
//                                    lon = location.getLongitude();
//                                }
//                            }
//                        }
//                    } catch (SecurityException e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } // end of try~catch
//        return location;
//    } // end of getLocation
//
//    // GPS OFF
//    public void stopUsingGPS() {
//        if (locationManager != null) {
//            locationManager.removeUpdates(GPS_to_Speed.this);
//        } // end of if
//    } // end of stopUsingGPS
//
//    public double getLatitude() {
//        if (location != null) {
//            lat = location.getLatitude();
//        } // end of if
//        return lat;
//    } // end of getLatitude
//
//    public double getLongitude() {
//        if (location != null) {
//            lon = location.getLongitude();
//        } // end of if
//        return lon;
//    } // end of getLatitude
//
//    public boolean isGetLocation() {
//        return this.isGetLocation;
//    } // end of isGetLocation
//
//    public void showSettingAlert() {
//        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
//        alertDialog.setTitle("GPS 사용유무");
//        alertDialog.setMessage("GPS 사용해야됨, 설정 창 ㄱ?");
//
//        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                mContext.startActivity(intent);
//            } // end of onClick
//        }); // end of setPositiveButton
//
//        alertDialog.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            } // end of onClick
//        }); // end of setNegativeButton
//        alertDialog.show();
//    } // end of showSettingAlert
//
//    @Override
//    public void onLocationChanged(Location location) {
//
//        Log.d("----Start location----", location.toString());
//        if (startTime == -1) {
//            startTime = location.getTime();
//        } // end of if
//        Log.i("time", String.valueOf(location.getTime()));
//        beforeLocation = getLocation();
//        float distance[] = new float[1];
//        Log.i("** Before Location", String.valueOf(beforeLocation.getLatitude()) + "!!!!" + String.valueOf(beforeLocation.getLongitude()));
//        Log.i("&& Current Location", String.valueOf(location.getLatitude()) + "!!!!" + String.valueOf(location.getLongitude()));
//        Location.distanceBetween(beforeLocation.getLatitude(), beforeLocation.getLongitude(), location.getLatitude(), location.getLongitude()
//                , distance); // distance -> meter m/s
//
//        float dis = distance[0];
////        Log.i("distance", distance.toString());
//        Log.i("*** distance ", String.valueOf(dis));
//        location.getSpeed();
//        Log.i("Speed", String.valueOf(location.getSpeed()));
//        long delay = location.getTime() - startTime;
//        double speed = distance[0] / delay;
//        double speedKMH = speed * 3600;  // m/s
//
//        beforeLocation = location;
//        Log.i("speed", String.valueOf(speedKMH));
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//}