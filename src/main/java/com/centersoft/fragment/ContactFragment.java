package com.centersoft.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.centersoft.ICallBack.IDataCallBack;
import com.centersoft.base.BaseActivity;
import com.centersoft.base.BaseListFragment;
import com.centersoft.base.ChatApplication;
import com.centersoft.chat.ChatActy;
import com.centersoft.chat.R;
import com.centersoft.effect.GlideCircleTransform;
import com.centersoft.entity.User;
import com.centersoft.enums.VFCode;
import com.centersoft.util.Constant;
import com.centersoft.util.MyLog;
import com.centersoft.util.NetTool;
import com.centersoft.util.Tools;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.centersoft.util.UiUtils.runOnUiThread;

/**
 * Created by liudong on 2017/7/18.
 */

public class ContactFragment extends BaseListFragment<User> {

    Socket socket;

    List<String> onLineUsers;

    @Override
    public int initViews() {
        return R.layout.fg_contact;
    }

    @Override
    protected void initData() {
        super.initData();
        onLineUsers = new ArrayList<>();
        socket = ChatApplication.getSocket();
        socket.on("onLine", new Emitter.Listener() {

            @Override
            public void call(Object... args) {      //上线
                org.json.JSONObject data = (org.json.JSONObject) args[0];
                final String name = data.optString("user");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Tools.showToast(name + "上线", context);
                        if (!onLineUsers.contains(name)) {
                            onLineUsers.add(name);
                        }
                        refList();
                    }
                });
            }

        }).on("offLine", new Emitter.Listener() {    //下线

            @Override
            public void call(Object... args) {
                org.json.JSONObject data = (org.json.JSONObject) args[0];
                final String name = data.optString("user");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Tools.showToast(name + "下线了", context);
                        onLineUsers.remove(name);
                        refList();
                    }
                });
            }

        });

        refreshLayout.setEnabled(false);
        comAdapter = new CommonAdapter<User>(context, R.layout.lay_online_user, listData) {
            @Override
            protected void convert(ViewHolder viewHolder, User item, int position) {
                ImageView imageView = viewHolder.getView(R.id.iv_headImage);
                Glide.with(context).load("http://qlogo2.store.qq.com/qzone/2355021" + position + "/2355021" + position + "/100")
                        .transform(new GlideCircleTransform(context)).into(imageView);

                viewHolder.setText(R.id.tv_name, item.getName())
                        .setText(R.id.tv_onLine, item.isOnline() ? "[在线]" : "[离线]");
            }
        };
        lv_list.setAdapter(comAdapter);


        lv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User u = listData.get(position);
                if (u.getName().equals(Constant.getLoginName())) {
                    Tools.showToast("不能与自己聊天", context);
                    return;
                }

                BaseActivity activity = (BaseActivity) context;
                Bundle bl = new Bundle();
                bl.putSerializable("user", u);
                activity.openActivity(ChatActy.class, bl);
            }
        });

    }

    @Override
    public void reqListData() {

        //获取当前在线用户的人
        NetTool.requestWithGet(context, Constant.allUsers, baseReqMap, new IDataCallBack<String>() {

            @Override
            public void closeDialog() {

            }

            @Override
            public void success(String result) {
                MyLog.i(Tag, result);
                JSONObject obj = JSON.parseObject(result).getJSONObject("data");
                showViewData(obj.getString("allUser"), User.class);
                onLineUsers = JSONArray.parseArray(obj.getString("onLineUsers"), String.class);
                refList();
            }

            @Override
            public void error(VFCode e) {

            }
        });
    }

    // 刷新列表
    private void refList() {
        for (User user : listData) {
            user.setOnline(onLineUsers.contains(user.getName()));
        }
        comAdapter.notifyDataSetChanged();
    }

}