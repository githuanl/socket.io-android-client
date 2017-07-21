package com.centersoft.util;

import android.text.TextUtils;

import com.blankj.utilcode.util.SPUtils;

/**
 * Created by liudong on 2017/7/5.
 */

public class Constant {

    public static final String BaseUrl = "http://192.168.0.33:3000";

    public static final String Login_Url = "mobileLogin";

    private static String Login_Name = "";

    public static String getLoginName(){
        if(TextUtils.isEmpty(Login_Name)){
            Login_Name = SPUtils.getInstance().getString("loginName");
        }
        return Login_Name;
    }

    private static String auth_Token = "";

    public static String getAuthToken(){
        if(TextUtils.isEmpty(auth_Token)){
            auth_Token = SPUtils.getInstance().getString("auth_token");
        }
        return auth_Token;
    }


    //注册用户
    public static final String Register_Url =    "register";

    public static final String onLineUsers = "onLineUsers";

    public static final String Upload_Files = "uploadFiles";

    public static final String allUsers = "allUsers";


}

