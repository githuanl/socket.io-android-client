package com.centersoft.chat;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.SPUtils;
import com.centersoft.ICallBack.IDataCallBack;
import com.centersoft.base.BaseActivity;
import com.centersoft.enums.VFCode;
import com.centersoft.util.AppManager;
import com.centersoft.util.Constant;
import com.centersoft.util.NetTool;
import com.centersoft.util.Tools;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 登录
 */
public class LoginActy extends BaseActivity {

    @BindView(R.id.ed_name)
    EditText edName;
    @BindView(R.id.ed_password)
    EditText edPassword;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.btn_register)
    FloatingActionButton btnRegister;

    @Override
    public int initResource() {
        return R.layout.acty_login;
    }

    @Override
    protected void initData() {
        super.initData();

        String loginname = SPUtils.getInstance().getString("loginName");
        String password = SPUtils.getInstance().getString("password");

        if (!TextUtils.isEmpty(loginname) && !TextUtils.isEmpty(password)) {
            edName.setText(loginname);
            edPassword.setText(password);
        }

    }

    // 登录 逻辑
    private void login() {
        String name = edName.getText().toString();
        String password = edPassword.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Tools.showToast("用户名不能为空", context);
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Tools.showToast("用户名不能为空", context);
            return;
        }

        baseDialog.show();
        baseReqMap.put("userName", name);
        baseReqMap.put("password", password);

        NetTool.requestWithPost(context, Constant.Login_Url, baseReqMap, new IDataCallBack<String>() {

            @Override
            public void closeDialog() {
                baseDialog.dismiss();
            }

            @Override
            public void success(String result) {
                JSONObject jobj = (JSONObject) JSONObject.parse(result);
                int code = jobj.getIntValue("code");
                if (code < 0) {
                    Tools.showToast(jobj.getString("message"), context);
                    return;
                } else {
                    SPUtils.getInstance().put("auth_token", jobj.getJSONObject("data").getString("auth_token"));
                    SPUtils.getInstance().put("loginName", baseReqMap.get("userName"));
                    SPUtils.getInstance().put("password", baseReqMap.get("password"));
                    openActivity(HomeActy.class);
                    AppManager.getInstance().killActivity(LoginActy.this);
                }
            }

            @Override
            public void error(VFCode e) {

            }
        });
    }

    @OnClick({R.id.btn_login, R.id.btn_register})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                login();
                break;
            case R.id.btn_register:

                getWindow().setExitTransition(null);
                getWindow().setEnterTransition(null);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options =
                            ActivityOptions.makeSceneTransitionAnimation(this, btnRegister, btnRegister.getTransitionName());
                    startActivity(new Intent(this, RegisterActy.class), options.toBundle());
                } else {
                    startActivity(new Intent(this, RegisterActy.class));
                }

                break;
        }
    }

}
