package com.save_us.safe_helmet;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

public class BackPressCloseHandler {
    private long backKeyPressedTime = 0;
    private Toast toast;
    private Activity activity;

    public BackPressCloseHandler(Activity context){
        this.activity = context;
    }

    public void onBackPressed(){
        if(System.currentTimeMillis() > backKeyPressedTime + 2000){
            backKeyPressedTime = System.currentTimeMillis();
            showGuide("뒤로 버튼을 한번 더 누르시면 종료됩니다.");
            return;
        }
        if(System.currentTimeMillis() <= backKeyPressedTime + 2000){
            Intent intent = new Intent(MainActivity.mContext,MyService.class);
            activity.stopService(intent);
            showGuide("앱이 종료됩니다.");
            activity.finish();
            toast.cancel();
        }
    }

    public void showGuide(String TEXT){
        toast = Toast.makeText(activity, TEXT, Toast.LENGTH_LONG);
        toast.show();
    }
}
