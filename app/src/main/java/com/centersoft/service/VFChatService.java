package com.centersoft.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;

import com.centersoft.base.ChatApplication;
import com.centersoft.util.Constant;
import com.centersoft.util.MyLog;
import com.coolerfall.daemon.Daemon;

public class VFChatService extends Service {

    private PowerManager.WakeLock wakeLock;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            if (!Constant.exit && !ChatApplication.getSocket().connected()) {
                ChatApplication.getSocket().open();
                MyLog.i("VFChatService", "重新连接");
            } else {
                MyLog.i("VFChatService", "已经连接");
            }
            ChatApplication.getHandler().postDelayed(runnable, 6000);
        }
    };


    private Context context;

    @Override
    public void onCreate() {

        super.onCreate();

        context = getApplicationContext();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, VFChatService.class.getName());
        wakeLock.acquire();

        Daemon.run(this, VFChatService.class, 30);
        ChatApplication.getHandler().post(runnable);

    }

    @Override
    public void onDestroy() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        super.onDestroy();
    }


    public IBinder onBind(Intent intent) {
        return null;
    }

}
