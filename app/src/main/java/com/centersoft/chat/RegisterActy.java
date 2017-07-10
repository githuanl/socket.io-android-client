package com.centersoft.chat;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.centersoft.ICallBack.IDataCallBack;
import com.centersoft.base.BaseActivity;
import com.centersoft.enums.VFCode;
import com.centersoft.util.Constant;
import com.centersoft.util.NetTool;
import com.centersoft.util.Tools;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 登录
 */
public class RegisterActy extends BaseActivity {

    @BindView(R.id.ed_name)
    EditText edName;
    @BindView(R.id.ed_password)
    EditText edPassword;

    @BindView(R.id.btn_register)
    Button btnRegister;

    @Override
    public int initResource() {
        return R.layout.acty_register;
    }


    // 注册 逻辑
    private void register() {
        String name = edName.getText().toString();
        String password = edPassword.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Tools.showToast("用户名不能为空", context);
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Tools.showToast("密码不能为空", context);
            return;
        }

        baseDialog.show();
        baseReqMap.put("userName", name);
        baseReqMap.put("password", password);

        NetTool.requestWithGet(context, Constant.Register_Url, baseReqMap, new IDataCallBack<String>() {

            @Override
            public void closeDialog() {
                baseDialog.dismiss();
            }

            @Override
            public void success(String result) {
                JSONObject jobj = (JSONObject) JSONObject.parse(result);
                int code = jobj.getIntValue("code");
                Tools.showToast(jobj.getString("message"), context);
                if (code < 0) {
                    return;
                } else {
                    goBack();
                }
            }

            @Override
            public void error(VFCode e) {

            }
        });

    }

    @OnClick({R.id.btn_register})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                register();
                break;
        }
    }

}
