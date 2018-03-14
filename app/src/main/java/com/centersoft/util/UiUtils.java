package com.centersoft.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.centersoft.base.ChatApplication;

/**
 * Description    ui 工具
 * FileName       UiUtils
 * CopyRight      CenterSoft
 * Author         LH
 * Createdate     2016/11/23 上午9:22
 * ------------------------------
 * updateAuthor   <修改人员>
 * updateDate     <修改日期>
 * updateNeedNum  <需求单号>
 * updateContent  <修改内容>
 * ------------------------------
 */
public class UiUtils {
    /**
     * 获取到字符数组
     *
     * @param tabNames 字符数组的id
     */
    public static String[] getStringArray(int tabNames) {
        return getResource().getStringArray(tabNames);
    }

    public static Resources getResource() {
        return ChatApplication.getApplication().getResources();
    }


    public static Context getContext() {
        return ChatApplication.getApplication();
    }

    /**
     * dip转换px
     */
    public static int dip2px(int dip) {
        final float scale = getResource().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    /**
     * px转换dip
     */

    public static int px2dip(int px) {
        final float scale = getResource().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    /**
     * 把Runnable 方法提交到主线程运行
     *
     * @param runnable
     */
    public static void runOnUiThread(Runnable runnable) {
        // 在主线程运行
        if (android.os.Process.myTid() == ChatApplication.getMainTid()) {
            runnable.run();
        } else {
            //获取handler
            ChatApplication.getHandler().post(runnable);
        }
    }

    public static int getColor(int color) {
        return ContextCompat.getColor(getContext(), color);
    }

    public static View inflate(int id) {
        return View.inflate(getContext(), id, null);
    }

    public static Drawable getDrawalbe(int id) {
        return ContextCompat.getDrawable(getContext(), id);
    }

    public static int getDimens(int homePictureHeight) {
        return (int) getResource().getDimension(homePictureHeight);
    }

    /**
     * 延迟执行 任务
     *
     * @param run  任务
     * @param time 延迟的时间
     */
    public static void postDelayed(Runnable run, int time) {
        ChatApplication.getHandler().postDelayed(run, time); // 调用Runable里面的run方法
    }

    /**
     * 取消任务
     *
     * @param auToRunTask
     */
    public static void cancel(Runnable auToRunTask) {
        ChatApplication.getHandler().removeCallbacks(auToRunTask);
    }

    /**
     * 设置 下划线
     *
     * @param tv
     */
    public static void setTextViewLine(String text, TextView tv) {
        if(StringUtils.isEmpty(text)){
            return;
        }
        tv.setText(text);
        tv.setTextColor(Color.parseColor("#378EF5"));
        tv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        tv.getPaint().setAntiAlias(true);//抗锯齿
        tv.getPaint().setColor(Color.parseColor("#378EF5"));
    }

}
