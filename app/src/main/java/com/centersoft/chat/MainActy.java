package com.centersoft.chat;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.centersoft.ICallBack.IDataCallBack;
import com.centersoft.base.BaseActivity;
import com.centersoft.base.ChatApplication;
import com.centersoft.entity.Bodies;
import com.centersoft.entity.VFMessage;
import com.centersoft.enums.Body_type;
import com.centersoft.enums.Chat_type;
import com.centersoft.enums.VFCode;
import com.centersoft.util.AppManager;
import com.centersoft.util.Constant;
import com.centersoft.util.GlideImageLoader;
import com.centersoft.util.MyLog;
import com.centersoft.util.NetTool;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.MultiItemTypeAdapter;
import com.zhy.adapter.abslistview.ViewHolder;
import com.zhy.adapter.abslistview.base.ItemViewDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActy extends BaseActivity {

    private static final int IMAGE_PICKER = 2;

    @BindView(R.id.lv_list)
    ListView lv_list;

    List<VFMessage> listData;
    MultiItemTypeAdapter adapter;


    @BindView(R.id.lv_list_right)
    ListView lv_list_right;

    List<String> onLineList;
    CommonAdapter onLineAdapter;


    @BindView(R.id.message_input)
    EditText messageInput;

    @BindView(R.id.btn_send_pic)
    Button btn_send_pic;

    Socket socket;

    @BindView(R.id.tv_touser)
    TextView tv_touser;

    @Override
    public int initResource() {
        return R.layout.acty_home;
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

    String toUser = "";

    @Override
    protected void initData() {


        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
        imagePicker.setShowCamera(true);  //显示拍照按钮
        imagePicker.setCrop(true);        //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true); //是否按矩形区域保存
        imagePicker.setSelectLimit(1);    //选中数量限制
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
        imagePicker.setFocusWidth(800);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000);//保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000);//保存文件的高度。单位像素


        listData = new ArrayList<>();
        adapter = new MultiItemTypeAdapter(this, listData);
        adapter.addItemViewDelegate(new LeftDelagate());
        adapter.addItemViewDelegate(new RightDelagate());
        lv_list.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        //当前在线
        onLineList = new ArrayList<>();
        onLineAdapter = new CommonAdapter<String>(this, R.layout.lay_online_user, onLineList) {
            @Override
            protected void convert(ViewHolder viewHolder, String item, int position) {
                viewHolder.setText(R.id.tv_name, item);
            }
        };
        lv_list_right.setAdapter(onLineAdapter);
        onLineAdapter.notifyDataSetChanged();
        lv_list_right.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = onLineList.get(position);
                if (name.equals("当前在线的人") || name.equals(Constant.Login_Name)) {
                    return;
                }
                if (name.equals(toUser)) {
                    toUser = "";
                    tv_touser.setText("给所有人发消息");
                    return;
                }
                toUser = name;
                tv_touser.setText("给" + toUser + "发消息");
            }
        });




        //获取当前在线用户的人
        NetTool.requestWithGet(context, Constant.onLineUsers, baseReqMap, new IDataCallBack<String>() {

            @Override
            public void closeDialog() {

            }

            @Override
            public void success(String result) {
                onLineList.add("当前在线的人");
                onLineList.addAll(JSON.parseArray(JSON.parseObject(result).getString("data"), String.class));
                onLineAdapter.notifyDataSetChanged();
                MyLog.i(Tag, "------" + result);
            }

            @Override
            public void error(VFCode e) {

            }
        });


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


                String result = args[0].toString();

                VFMessage msg = JSON.parseObject(result, VFMessage.class);

                if (!msg.getFrom().equals(Constant.Login_Name)) {

                    MediaPlayer player = MediaPlayer.create(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.setLooping(false);    //设置是否循环播放
                    player.setVolume(1.0f, 1.0f);
                    player.start();

                    listData.add(msg);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                adapter.notifyDataSetChanged();
                            } catch (Exception e) {

                            }
                        }
                    });

                }

            }

        }).on("liaotian", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                try {

                    final JSONObject data = (JSONObject) args[0];

                    if (!data.getString("ren").equals(Constant.Login_Name)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                String message = "";
                                try {
                                    message = data.getString("neirong");
                                    VFMessage msg = new VFMessage(data.getString("ren"), "所有人", Chat_type.chat,
                                            new Bodies(Body_type.txt, message));
                                    listData.add(msg);
                                    adapter.notifyDataSetChanged();
                                    lv_list.setSelection(listData.size() - 1);
                                } catch (JSONException e) {

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
                if (socket != null && !socket.connected()) { //重新连接
                    socket.connect();
                    return;
                }
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
                if (socket != null && !socket.connected()) { //重新连接
                    socket.connect();
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActy.this, "重新连接", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }).on(Socket.EVENT_ERROR, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActy.this, "错误发生，并且无法被其他事件类型所处理", Toast.LENGTH_SHORT).show();
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
            if (vfMessage.getBodies().getType() == Body_type.img) {
                holder.getView(R.id.iv_image).setVisibility(View.VISIBLE);
                holder.getView(R.id.tv_text).setVisibility(View.GONE);
                String imageUrl = Constant.BaseUrl + "/" + vfMessage.getBodies().getImgUrl();
                Glide.with(context)
                        .load(imageUrl)
                        .into((ImageView) holder.getView(R.id.iv_image));
            } else {
                holder.getView(R.id.iv_image).setVisibility(View.GONE);
                holder.getView(R.id.tv_text).setVisibility(View.VISIBLE);
                holder.setText(R.id.tv_name, vfMessage.getFrom())
                        .setText(R.id.tv_text, vfMessage.getBodies().getMsg());
            }
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

            if (vfMessage.getBodies().getType() == Body_type.img) {
                holder.getView(R.id.iv_image).setVisibility(View.VISIBLE);
                holder.getView(R.id.tv_text).setVisibility(View.GONE);
                String imageUrl = Constant.BaseUrl + "/" + vfMessage.getBodies().getImgUrl();
                Glide.with(context)
                        .load(imageUrl)
                        .into((ImageView) holder.getView(R.id.iv_image));
            } else {
                holder.getView(R.id.iv_image).setVisibility(View.GONE);
                holder.getView(R.id.tv_text).setVisibility(View.VISIBLE);
                holder.setText(R.id.tv_name, vfMessage.getFrom())
                        .setText(R.id.tv_text, vfMessage.getBodies().getMsg());
            }
        }

    }


    public void sendMessage(Body_type type, String imageUrl) {
        try {

            String text = messageInput.getText().toString();

            if (TextUtils.isEmpty(text) && type == Body_type.txt) {
                return;
            }
            JSONObject obj = new JSONObject();
            VFMessage message = null;
            if (!TextUtils.isEmpty(toUser)) {


                String to = toUser;

                obj.put("from", Constant.Login_Name);
                obj.put("to", to);
                obj.put("chat_type", "chat");

                JSONObject bodiesObj = new JSONObject();
                bodiesObj.put("type", type.toString());
                switch (type) {
                    case txt:
                        bodiesObj.put("msg", text);
                        break;
                    case img:
                        bodiesObj.put("imgUrl", imageUrl);
                        break;
                }

                obj.put("bodies", bodiesObj);
                socket.emit("chat", obj);

                message = JSON.parseObject(obj.toString(), VFMessage.class);

            } else {      //群聊
                obj.put("ren", Constant.Login_Name);
                obj.put("neirong", text);
                socket.emit("liaotian", obj);
                message = new VFMessage(Constant.Login_Name, "所有人", Chat_type.chat,
                        new Bodies(type, text));
            }

            messageInput.setText("");

            listData.add(message);
            adapter.notifyDataSetChanged();

        } catch (JSONException e) {

        }
    }


    @OnClick({R.id.send_button, R.id.btn_send_pic})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_button:
                sendMessage(Body_type.txt, "");
                break;
            case R.id.btn_send_pic:
                Intent intent = new Intent(this, ImageGridActivity.class);
                startActivityForResult(intent, IMAGE_PICKER);
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == IMAGE_PICKER) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                final ImageItem im = images.get(0);

                baseDialog.show();
                baseReqMap.clear();

                Map<String, String> map = new HashMap<>();
                baseReqMap.put("image", im.path);
                map.put("file_submit", JSON.toJSONString(baseReqMap));
                map.put("auth_token", Constant.Auth_Token);
                //获取当前在线用户的人
                NetTool.requestWithPost(context, Constant.Upload_Files, map, new IDataCallBack<String>() {

                    @Override
                    public void closeDialog() {
                        baseDialog.dismiss();
                    }

                    @Override
                    public void success(String result) {
                        com.alibaba.fastjson.JSONObject obj = JSON.parseObject(result);

                        if (obj.getIntValue("code") > 0) { //上传成功
                            MyLog.i("imageurl", obj.getString("data"));
                            sendMessage(Body_type.img, obj.getString("data"));
                        }

                    }

                    @Override
                    public void error(VFCode e) {
                        MyLog.i(Tag, "err --" + e);
                    }
                });


            } else {
                Toast.makeText(this, "没有数据", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!socket.connected()) { //示连接则重新连接
            socket.connect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.close();
        AppManager.getInstance().AppExit(context);
    }
}
