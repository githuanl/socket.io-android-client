package com.centersoft.chat;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.centersoft.base.BaseActivity;
import com.centersoft.base.ChatApplication;
import com.centersoft.entity.VFMessage;
import com.centersoft.util.Constant;
import com.zhy.adapter.abslistview.MultiItemTypeAdapter;
import com.zhy.adapter.abslistview.ViewHolder;
import com.zhy.adapter.abslistview.base.ItemViewDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActy extends BaseActivity {

    @BindView(R.id.lv_list)
    ListView lv_list;
    List<VFMessage> listData;
    MultiItemTypeAdapter adapter;


    @BindView(R.id.message_input)
    EditText messageInput;

    Socket socket;


    @Override
    public int initResource() {
        return R.layout.activity_main;
    }

    @Override
    public boolean hasBackButton() {
        return false;
    }

    @Override
    protected void setMyToolBar() {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle("群聊");
    }

    @Override
    protected void initData() {

        listData = new ArrayList<>();
        adapter = new MultiItemTypeAdapter(this, listData);
        adapter.addItemViewDelegate(new LeftDelagate());
        adapter.addItemViewDelegate(new RightDelagate());
        lv_list.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        socket = ChatApplication.getSocket();

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActy.this, "连接成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).on("chat", new Emitter.Listener() {

            @Override
            public void call(Object... args) {

                try {

                    JSONObject data = (JSONObject) args[0];

                    if (!data.optString("from").equals(Constant.Login_Name)) {

                        MediaPlayer player = MediaPlayer.create(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        player.setLooping(false);    //设置是否循环播放
                        player.setVolume(1.0f, 1.0f);
                        player.start();


                        String message = data.getJSONObject("bodies").optString("msg");
                        VFMessage msg = new VFMessage(data.optString("from"), data.optString("to"), VFMessage.Chat_type.chat,
                                new VFMessage.Bodies(VFMessage.Body_type.txt, message));
                        listData.add(msg);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    adapter.notifyDataSetChanged();
                                    lv_list.setSelection(listData.size() - 1);
                                } catch (Exception e) {
                                }
                            }
                        });

                    }
                } catch (JSONException e) {
                }
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActy.this, "连接关闭", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActy.this, "连接失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });

        socket.connect();
    }

    /**
     *
     */
    public class LeftDelagate implements ItemViewDelegate<VFMessage> {

        @Override
        public int getItemViewLayoutId() {
            return R.layout.lay_message_left;
        }

        @Override
        public boolean isForViewType(VFMessage item, int position) {
            return !item.getFrom().equals(Constant.Login_Name);
        }

        @Override
        public void convert(ViewHolder holder, VFMessage vfMessage, int position) {
            holder.setText(R.id.tv_name, vfMessage.getFrom())
                    .setText(R.id.tv_text, vfMessage.getBodies().getMsg());
        }
    }

    /**
     *
     */
    public class RightDelagate implements ItemViewDelegate<VFMessage> {

        @Override
        public int getItemViewLayoutId() {
            return R.layout.lay_message_right;
        }

        @Override
        public boolean isForViewType(VFMessage item, int position) {
            return item.getFrom().equals(Constant.Login_Name);
        }

        @Override
        public void convert(ViewHolder holder, VFMessage vfMessage, int position) {
            holder.setText(R.id.tv_name, vfMessage.getFrom())
                    .setText(R.id.tv_text, vfMessage.getBodies().getMsg());
        }

    }

    @OnClick({R.id.send_button})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_button:

                try {

                    String text = messageInput.getText().toString();

                    if (TextUtils.isEmpty(text)) {
                        return;
                    }

                    String to = "2";

                    JSONObject obj = new JSONObject();
                    obj.put("from", Constant.Login_Name);
                    obj.put("to", to);
                    obj.put("chat_type", "chat");

                    JSONObject bodiesObj = new JSONObject();
                    bodiesObj.put("type", "txt");
                    bodiesObj.put("msg", text);
                    obj.put("bodies", bodiesObj);

                    socket.emit("chat", obj);

                    messageInput.setText("");

                    VFMessage message = new VFMessage(Constant.Login_Name, to, VFMessage.Chat_type.chat,
                            new VFMessage.Bodies(VFMessage.Body_type.txt, text));
                    listData.add(message);
                    adapter.notifyDataSetChanged();
                    lv_list.setSelection(listData.size() - 1);

                } catch (JSONException e) {

                }

                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.test, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!socket.connected()) { //示连接则重新连接
            socket.connect();
        }
    }
}
