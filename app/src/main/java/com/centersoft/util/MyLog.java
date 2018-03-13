package com.centersoft.util;

import android.util.Log;

/**
 * Description    日志控制
 * FileName       MyLog.java
 * CopyRight      CenterSoft
 * Author         LH
 * Createdate     2015年6月4日 下午3:31:45
 * ------------------------------
 * updateAuthor   <修改人员>
 * updateDate     <修改日期>
 * updateNeedNum  <需求单号>
 * updateContent  <修改内容>
 * ------------------------------
 */
public class MyLog {

    private static final boolean ISDEBUG = true;    //控制输出日志

    public static void d(String tag, String msg) {
        if (ISDEBUG) Log.d(tag, msg);
    }

    public static void v(String tag, String msg) {
        if (ISDEBUG) Log.v(tag, msg);
    }

    public static void i(String msg) {

        if (ISDEBUG) Log.i("Mylog====>", msg);
    }

    public static void i(String tag, String msg) {

//		if(ISDEBUG) KLog.json(msg);
        if (ISDEBUG) Log.i(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (ISDEBUG) Log.e(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (ISDEBUG) Log.w(tag, msg);
    }

    public static void wtf(String tag, String msg) {
        if (ISDEBUG) Log.wtf(tag, msg);
    }
}
