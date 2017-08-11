package com.centersoft.ICallBack;

import com.centersoft.enums.VFCode;

/**
 * Created by liudong on 2017/7/5.
 */

public abstract class IDataCallBack<T> {

    //成功
    public abstract void success(T result);

    //失败
    public abstract void error(VFCode e);

    //关闭
    public abstract void closeDialog();

    //失败
    public void error() {
    }

    //执行前
    public void onBefore() {
    }

    //执行后
    public void onAfter() {
    }

    //取消
    public void onCancle() {
    }


    public void onCancle(T result) {
    }


    public void inProgress(float progress, long total, int id) {
    }


}
