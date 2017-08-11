package com.centersoft.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.blankj.utilcode.util.Utils;
import com.centersoft.util.Constant;
import com.centersoft.util.MyLog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.cookie.CookieJarImpl;
import com.zhy.http.okhttp.cookie.store.PersistentCookieStore;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.centersoft.util.Constant.auth_Token;


public class ChatApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private static Socket socket;

    private static ChatApplication application;

    private static int mainTid;
    private static Handler handler;

    public static final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            ChatApplication.getSocket().open();
            handler.postDelayed(runnable, 6000);
        }
    };

    public static void Run() {
        handler.postDelayed(runnable, 6000);
    }

    public static Socket getSocket() {
        if (socket == null) {
            IO.Options opts = new IO.Options();
            opts.forceNew = false;
            opts.reconnection = true;
            opts.reconnectionDelay = 2000;      //延迟
            opts.reconnectionDelayMax = 6000;
            opts.reconnectionAttempts = -1;
            opts.timeout = 6000;
            opts.query = "auth_token=" + auth_Token;
            try {
                socket = IO.socket(Constant.BaseUrl, opts);
                return socket;
            } catch (Exception e) {
            }
        }
        return socket;
    }

    public static void closeSocket() {
        socket = null;
    }


    @Override
    public void onCreate() {

        super.onCreate();


        registerActivityLifecycleCallbacks(this);

        Utils.init(getApplicationContext());

        application = this;
        mainTid = android.os.Process.myTid();
        handler = new Handler();

        // CookieJarImpl cookieJar = new CookieJarImpl(new MemoryCookieStore());
        CookieJarImpl cookieJar = new CookieJarImpl(new PersistentCookieStore(getApplicationContext()));
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                //.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.0.33", 8888)))
                //.addInterceptor(new LoggerInterceptor("TAG"))
                .connectionPool(new ConnectionPool(8, 5, TimeUnit.MINUTES))   //连接默认为5分钟 第一个最大连接数 第二个为保持连接的时间
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        //取消长连接
                        //request = request.newBuilder().addHeader("Connection", "close").build();
                        request = request.newBuilder().addHeader("Connection", "keep-alive").build();
                        return chain.proceed(request);
                    }
                })
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .build();
        OkHttpUtils.initClient(okHttpClient);

    }

    public static Context getApplication() {
        return application;
    }

    public static int getMainTid() {
        return mainTid;
    }

    public static Handler getHandler() {
        return handler;
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (isBackGround) {
            isBackGround = false;
            MyLog.i("bo", "APP回到了前台");
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    private boolean isBackGround = true;

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            isBackGround = true;
            MyLog.i("bo", "APP遁入后台");
        }
    }
}
