package com.centersoft.base;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.blankj.utilcode.util.Utils;
import com.centersoft.util.Constant;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.cookie.CookieJarImpl;
import com.zhy.http.okhttp.cookie.store.PersistentCookieStore;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;



public class ChatApplication extends Application {

    private static Socket socket;

    private static ChatApplication application;


    private static int mainTid;
    private static Handler handler;

    public static Socket getSocket() {
        if (socket == null) {
            IO.Options opts = new IO.Options();
            opts.forceNew = false;
            opts.reconnection = true;
            opts.reconnectionDelay = 2000;      //延迟
            opts.reconnectionDelayMax = 8000;   //延迟
            opts.reconnectionAttempts = -1;
            opts.timeout = 5000;
            opts.query = "auth_token=" + Constant.getAuthToken();
            try {
                socket = IO.socket(Constant.BaseUrl, opts);
                return socket;
            } catch (URISyntaxException e) {
                return null;
            }
        } else {
            return socket;
        }
    }

    @Override
    public void onCreate() {

        super.onCreate();

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




}
