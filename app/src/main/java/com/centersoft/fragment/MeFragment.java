package com.centersoft.fragment;

import android.view.View;
import android.widget.EditText;

import com.centersoft.base.BaseDetailFragment;
import com.centersoft.base.ChatApplication;
import com.centersoft.chat.R;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.OnClick;
import io.socket.client.Ack;
import io.socket.client.Socket;

/**
 * Created by liudong on 2017/7/18.
 */

public class MeFragment extends BaseDetailFragment {

    @Override
    public int initViews() {
        return R.layout.fg_me;
    }

    Socket socket;

    @BindView(R.id.text)
    EditText text;

    @BindView(R.id.roomName)
    EditText roomName;

    // 初始化
    protected void initData() {
        socket = ChatApplication.getSocket();
    }

    @OnClick({R.id.add_room, R.id.leave_room, R.id.send_msg, R.id.create_room})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_room:
                try {
                    JSONObject object = new JSONObject();
                    object.put("roomId", roomName.getText().toString());
                    socket.emit("create", object, new Ack() {

                        @Override
                        public void call(Object... args) {

                        }
                    });

                } catch (JSONException e) {

                }
                break;
            case R.id.add_room:
                try {
                    JSONObject object = new JSONObject();
                    object.put("roomId", roomName.getText().toString());
                    socket.emit("join", object, new Ack() {
                        @Override
                        public void call(Object... args) {

                        }
                    });
                } catch (JSONException e) {

                }
                break;
            case R.id.leave_room:
                try {
                    JSONObject object = new JSONObject();
                    object.put("roomId", roomName.getText().toString());
                    socket.emit("leave", object, new Ack() {
                        @Override
                        public void call(Object... args) {

                        }
                    });
                } catch (JSONException e) {
                }
                break;
            case R.id.send_msg:
                try {
                    JSONObject object = new JSONObject();
                    object.put("roomId", roomName.getText().toString());
                    object.put("say", text.getText().toString());
                    socket.emit("GroupChat", object, new Ack() {
                        @Override
                        public void call(Object... args) {

                        }
                    });
                } catch (JSONException e) {

                }
                break;
        }
    }

}
