package com.centersoft.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import com.centersoft.base.ChatApplication;
import com.coolerfall.daemon.Daemon;

import static com.centersoft.base.ChatApplication.runnable;

public class VFChatService extends Service {

    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {

        super.onCreate();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, VFChatService.class.getName());
        wakeLock.acquire();

        Daemon.run(this, VFChatService.class, 30);
        ChatApplication.Run();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(30, new Notification());
        startService(new Intent(this, FakeService.class));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        ChatApplication.getHandler().removeCallbacks(runnable);
        super.onDestroy();
    }

    public class FakeService extends Service {
        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(30, new Notification());
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public void onDestroy() {
            stopForeground(true);
            super.onDestroy();
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

}
