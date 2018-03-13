package com.centersoft.base;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.centersoft.chat.R;
import com.centersoft.entity.EventBusType;
import com.centersoft.util.AppManager;
import com.centersoft.util.MyLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public abstract class BaseActivity extends AppCompatActivity {

    protected String Tag = BaseActivity.class.getSimpleName();

    protected Unbinder unbinder;

    //请求数据map
    protected Map<String, String> baseReqMap = new HashMap<String, String>();

    protected Context context;

    protected ProgressDialog baseDialog;

    @Nullable
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @Nullable
    @BindView(R.id.toobar_title)
    protected TextView toobarTitle;


    protected ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

        super.onCreate(savedInstanceState);
        beforeInitResource();
        setContentView(initResource());


        init();
        initData();
        showView();
    }

    protected void init() {

        context = this;

        unbinder = ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        AppManager.getInstance().addActivity(this);

        actionBar = getSupportActionBar();
        if (actionBar == null) {
            setMyToolBar();
            if (toobarTitle != null) {
                actionBar.setTitle("");
            }
        }

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(hasBackButton());
            actionBar.setHomeButtonEnabled(true);
            actionBar.setElevation(0);              //去阴影
        }

        baseDialog = new ProgressDialog(context);
        baseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        baseDialog.setCanceledOnTouchOutside(false);
        baseDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        baseDialog.setMessage("加载数据中...");

    }

    /**
     * 是否有返回按钮
     */
    protected boolean hasBackButton() {
        return true;
    }

    // 设置toobar
    protected void setMyToolBar() {
    }

    /**
     * 初始化布局 前
     */
    protected void beforeInitResource() {
    }

    /**
     * 初始化布局资源文件
     */
    protected abstract int initResource();

    /**
     * 初始化数据
     */
    protected void initData() {

    }

    /**
     * 显示界面
     */
    protected void showView() {
    }

    protected <T extends View> T $(View v, int resId) {
        return (T) v.findViewById(resId);
    }

    protected <T extends View> T $(int resId) {
        return (T) findViewById(resId);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
        EventBus.getDefault().unregister(this);//取消订阅
    }

    //结果
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResult(EventBusType data) {

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:// 点击bar action 返回图标事件
                goBack();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack();
        }
        return true;
    }

    /**
     * 返回前做处理
     */
    public boolean beforeBack() {
        return true;
    }

    /**
     * @throws
     * @Title 返回
     * @Description TODO
     * @author LH
     */
    public void goBack() {
        if (!beforeBack()) {
            return;
        }
        AppManager.getInstance().killActivity(this);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    /**
     * 通过类名启动Activity
     *
     * @param pClass
     */
    public void openActivity(Class<?> pClass) {
        openActivity(pClass, null);
    }

    /**
     * 通过类名启动Activity，并且含有Bundle数据
     *
     * @param pClass
     * @param pBundle
     */
    public void openActivity(Class<?> pClass, Bundle pBundle) {
        Intent intent = new Intent(this, pClass);
        if (pBundle != null) {
            intent.putExtras(pBundle);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.pre_fade_in, R.anim.pre_fade_out);
    }

    protected void openActivityForResult(Class<?> pClass, Bundle pBundle) {
        Intent intent = new Intent(this, pClass);
        if (pBundle != null) {
            intent.putExtras(pBundle);
        }
        startActivityForResult(intent, 0);
        overridePendingTransition(R.anim.pre_fade_in, R.anim.pre_fade_out);
    }


    public static <T> List<T> parseArray(String json, Class<T> cla) {
        return JSON.parseArray(json, cla);
    }


    public static <T> T parseObject(String json, Class<T> cla) {
        return JSON.parseObject(json, cla);
    }


    //控制点击其他位置时关闭键盘
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    protected boolean isActive = true; //是否活跃

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        if (!isActive) {
            //app 从后台唤醒，进入前台
            isActive = true;
            MyLog.i(Tag, "进入前台");
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();

        if (!isAppOnForeground()) {
            //app 进入后台
            //全局变量 记录当前已经进入后台
            isActive = false;
            MyLog.i(Tag, "进入后台");
        }
    }

    /**
     * 程序是否在前台运行
     *
     * @return
     */
    public boolean isAppOnForeground() {
        // Returns a list of application processes that are running on the
        // device

        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }


    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        MyLog.i("memory -- info -->", level + "");
    }
}
