package com.centersoft.util;

import android.content.Context;

import com.centersoft.ICallBack.IDataCallBack;
import com.centersoft.enums.VFCode;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;


/***
 * 网络请求封装
 *
 * @author Comsys-liudong
 * @ClassName: NetTool
 * @Description: TODO
 * @date 2016-8-1 下午4:51:32
 */
public class NetTool {


    public enum RequestType {
        Get, Post
    }

    /**
     * post 请求
     *
     * @param context
     * @param urlString
     * @param parameters
     * @param callBack
     */
    public static void requestWithPost(Context context, String urlString, Map<String, String> parameters, IDataCallBack callBack) {
        ba_requestWithType(RequestType.Post, context, urlString, parameters, callBack);
    }

    /**
     * Get 请求
     *
     * @param context
     * @param urlString
     * @param parameters
     * @param callBack
     */
    public static void requestWithGet(Context context, String urlString, Map<String, String> parameters, IDataCallBack callBack) {
        ba_requestWithType(RequestType.Get, context, urlString, parameters, callBack);
    }

    /**
     * Get post 请求数据
     *
     * @param type       请求类型
     * @param context
     * @param urlString  请求的url
     * @param parameters 参数Map
     * @param callBack   回调
     */
    private static void ba_requestWithType(RequestType type, Context context, String urlString, Map<String, String> parameters,
                                           IDataCallBack callBack) {
        if (!Tools.isConnected(context)) {
            callBack.error(VFCode.REQ_NONETWORK);
            Tools.showToast("网络未连接!", context);
            return;
        }
        String url = Constant.BaseUrl + "/" + urlString;
        switch (type) {
            case Get:
                OkHttpUtils.get()
                        .tag(context)
                        .url(url)
                        .params(parameters)
                        .build()
                        .execute(callBackManage(type, context, urlString, parameters, callBack));
                break;
            case Post:
                OkHttpUtils.post()
                        .tag(context)
                        .url(url)
                        .params(parameters)
                        .build()
                        .execute(callBackManage(type, context, urlString, parameters, callBack));
                break;
        }
    }


    private abstract static class ReqCallback extends Callback<String> {

        @Override
        public String parseNetworkResponse(Response response, int id) throws IOException {
            return response.body().string();
        }

    }


    private static ReqCallback callBackManage(final RequestType type, final Context context,
                                              final String urlString,
                                              final Map<String, String> parameters,
                                              final IDataCallBack callBack) {

        return new ReqCallback() {

            @Override
            public void onError(Call call, Exception e, int id) {

                String message = e.getMessage();
                MyLog.i("NetTool-error----->", message);

                callBack.error();
                callBack.closeDialog();
            }

            @Override
            public void onResponse(String result, int id) {
                callBack.closeDialog();

                callBack.success(result);
            }
        };
    }

}
