package com.centersoft.chat;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.bumptech.glide.Glide;
import com.centersoft.ICallBack.VoiceRecordCompleteCallback;
import com.centersoft.base.BaseActivity;
import com.centersoft.base.ChatApplication;
import com.centersoft.db.dao.ConversationDao;
import com.centersoft.db.dao.VFMessageDao;
import com.centersoft.effect.VFListView;
import com.centersoft.effect.VFSwipeRefreshLayout;
import com.centersoft.entity.Bodies;
import com.centersoft.entity.EventBusType;
import com.centersoft.entity.User;
import com.centersoft.entity.VFMessage;
import com.centersoft.enums.Body_type;
import com.centersoft.fragment.EmojiFragment;
import com.centersoft.util.Constant;
import com.centersoft.util.EBConstant;
import com.centersoft.util.EmotionUtil;
import com.centersoft.util.GlideImageLoader;
import com.centersoft.util.KeyboardStatusDetector;
import com.centersoft.util.Tools;
import com.centersoft.util.UiUtils;
import com.centersoft.util.ViewPageAdapter;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.MultiItemTypeAdapter;
import com.zhy.adapter.abslistview.ViewHolder;
import com.zhy.adapter.abslistview.base.ItemViewDelegate;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.socket.client.Ack;
import io.socket.client.Socket;
import okhttp3.Call;

import static com.centersoft.util.Constant.chatToUser;

//聊天界面
public class ChatActy extends BaseActivity implements VoiceRecordCompleteCallback {

    private static final int IMAGE_PICKER = 2;

    @BindView(R.id.refresh_view)
    protected VFSwipeRefreshLayout refreshLayout;

    @BindView(R.id.lv_list)
    protected VFListView lv_list;

    protected List<VFMessage> listData = new ArrayList();

    protected int offset = 0;                    //动态加载数据 开始
    protected int limit = 20;                   //动态加载数据 一次加载20条数据

    protected Boolean loadMoreData = true;      // 是否加载更多

    MultiItemTypeAdapter adapter;

    @BindView(R.id.message_input)
    EditText messageInput;

    Socket socket;

    @BindView(R.id.gridView)
    GridView gridView;

    @BindView(R.id.send_button)
    Button send_button;

    @BindView(R.id.btn_send_pic)
    ImageButton btn_send_pic;

    @BindView(R.id.include_more)
    View include_more;

    @BindView(R.id.include_voice_view)
    View include_voice_view;


    @BindView(R.id.include_emoji)
    View include_emoji;

    @BindView(R.id.popVoice)
    CheckBox popVoice;

    @BindView(R.id.popEmoji)
    CheckBox popEmoji;

    @BindView(R.id.viewpager)
    ViewPager emjViewpager;

    static enum KeyboardAdd {

        photo("相册", R.drawable.keyboard_add_photo, ImageGridActivity.class),

        camera("拍照", R.drawable.keyboard_add_camera, CameraActy.class),

        location("位置", R.drawable.keyboard_add_location, LocationActy.class),

        video("视频", R.drawable.keyboard_add_video, VideoChatActy.class);

        private int res;
        private String name;
        private Class jumpActy;

        private KeyboardAdd(String name, int res, Class jumpActy) {
            this.name = name;
            this.res = res;
            this.jumpActy = jumpActy;
        }

        public int getDwRes() {
            return res;
        }

        public String getName() {
            return name;
        }

        public Class getJumpActy() {
            return jumpActy;
        }


    }

    @Override
    public int initResource() {
        return R.layout.acty_chat;
    }

    @Override
    public boolean hasBackButton() {
        return true;
    }

    @Override
    protected void setMyToolBar() {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
    }


    String toUser = "";
    User user;

    VFMessageDao messageDao;
    ConversationDao conversationDao;

    //发送语音
    @Override
    public void recordFinished(long duration, String voicePath) {


        try {
            JSONObject obj = new JSONObject();

            obj.put("from_user", Constant.Login_Name);
            obj.put("to_user", toUser);
            obj.put("chat_type", "chat");

            JSONObject bodiesObj = new JSONObject();
            bodiesObj.put("type", "audio");
            bodiesObj.put("duration", duration / 1000);
            byte[] fileByte = File2byte(voicePath);
            bodiesObj.put("fileData", fileByte);
            bodiesObj.put("fileName", voicePath.replace(Environment.getExternalStorageDirectory().getPath() + "/luanliao/", ""));
            obj.put("bodies", bodiesObj);

            socket.emit("chat", obj, new Ack() {

                @Override
                public void call(Object... args) {

                    try {
                        if (args.length > 1) {      //给 服务器发回调 客户端收到消息了
                            Ack ack = (Ack) args[args.length - 1];
                            ack.call();
                        }

                        JSONObject obj = (JSONObject) args[0];
                        String bod = obj.getString("bodies");
                        Bodies bodies = JSON.parseObject(bod, Bodies.class);
                        obj.remove("bodies");

                        VFMessage msg = JSON.parseObject(obj.toString(), VFMessage.class);
                        msg.setBodies(bodies);

                        listData.add(msg);
                        messageDao.saveMessage(msg);
                        conversationDao.saveOrUpdateConversation(msg);

                        UiUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    adapter.notifyDataSetChanged();
                                    lv_list.setSelection(listData.size() - 1);
                                } catch (Exception e) {
                                }
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private MediaPlayer mMyMediaPlayer;

    @Override
    protected void initData() {


        socket = ChatApplication.getSocket();
        messageDao = new VFMessageDao();
        conversationDao = new ConversationDao();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("user")) {
                user = (User) bundle.getSerializable("user");
                toobarTitle.setText(user.getName());
                toUser = user.getName();
            } else if (bundle.containsKey("userName")) {
                String userName = bundle.getString("userName");
                toobarTitle.setText(userName);
                toUser = userName;
            }
        }

        chatToUser = toUser;

        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
        imagePicker.setShowCamera(true);  //显示拍照按钮
        imagePicker.setCrop(true);        //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true); //是否按矩形区域保存
        imagePicker.setSelectLimit(5);    //选中数量限制
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

        lv_list.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyboardUtils.hideSoftInput(ChatActy.this);
                return false;
            }
        });


        lv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                VFMessage vfMessage = listData.get(position);
                switch (vfMessage.getBodies().getType()) {
                    case audio:
                        final String path = vfMessage.getBodies().getFileRemotePath().replace("audios/", "");
                        final String sdPath = Environment.getExternalStorageDirectory().getPath() + "/luanliao/";

                        if (mMyMediaPlayer == null) {
                            mMyMediaPlayer = new MediaPlayer();
                        } else {
                            mMyMediaPlayer.reset();
                        }

                        if (FileUtils.isFileExists(sdPath + path)) {        //文件已经下载
                            try {
                                mMyMediaPlayer.setDataSource(sdPath + path);
                                mMyMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                mMyMediaPlayer.prepare();
                                mMyMediaPlayer.start();
                            } catch (IOException e) {
                            }
                        } else {                                            //去下载
                            OkHttpUtils//
                                    .get()//
                                    .tag(context)
                                    .url(Constant.BaseUrl + "/" + vfMessage.getBodies().getFileRemotePath())//
                                    .build()//
                                    .execute(new FileCallBack(sdPath, path) {
                                        @Override
                                        public void onError(Call call, Exception e, int id) {

                                        }

                                        @Override
                                        public void onResponse(File response, int id) {
                                            try {
                                                mMyMediaPlayer.setDataSource(sdPath + path);
                                                mMyMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                                mMyMediaPlayer.prepare();
                                                mMyMediaPlayer.start();
                                            } catch (IOException e) {
                                            }
                                        }
                                    });
                        }

                        break;
                }
            }
        });


        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        new KeyboardStatusDetector()
                .registerView(rootView)  //or register to an activity
                .setVisibilityListener(new KeyboardStatusDetector.KeyboardVisibilityListener() {
                    @Override
                    public void onVisibilityChanged(boolean keyboardVisible) {
                        if (keyboardVisible) {
                            lv_list.setSelection(listData.size() - 1 > 0 ? listData.size() - 1 : 0);
                        } else {
                            include_more.setVisibility(View.GONE);
                            include_emoji.setVisibility(View.GONE);
                            include_voice_view.setVisibility(View.GONE);
                            popEmoji.setChecked(false);
                            popVoice.setChecked(false);
                        }
                    }
                });

        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    btn_send_pic.setVisibility(View.GONE);
                    send_button.setVisibility(View.VISIBLE);
                } else {
                    btn_send_pic.setVisibility(View.VISIBLE);
                    send_button.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        messageInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage(Body_type.txt);
                    return true;
                }
                return false;
            }
        });

        popVoice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                include_more.setVisibility(View.GONE);
                include_emoji.setVisibility(View.GONE);
                include_voice_view.setVisibility(include_voice_view.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                popEmoji.setChecked(false);
            }
        });

        popEmoji.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                include_more.setVisibility(View.GONE);
                include_voice_view.setVisibility(View.GONE);
                include_emoji.setVisibility(include_emoji.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                popVoice.setChecked(false);
            }
        });


        //将其设置未读数为0
        conversationDao.updateConversationUnReadNum(Constant.Login_Name, toUser);

        List<VFMessage> ms = messageDao.getMessageListWithToUser(Constant.Login_Name, toUser, offset, limit);
        loadMoreData = ms.size() == limit;
        listData.addAll(ms);
        adapter.notifyDataSetChanged();

        lv_list.setSelection(listData.size() - 1 > 0 ? listData.size() - 1 : 0);


        refreshLayout.setColorSchemeResources(R.color.colorPrimaryDark, R.color.orange_500, R.color.green_500,
                R.color.colorPrimaryDark);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (loadMoreData) {
                    offset += limit;

                    List<VFMessage> ms = messageDao.getMessageListWithToUser(Constant.Login_Name, toUser, offset, limit);
                    loadMoreData = ms.size() == limit;
                    listData.addAll(0, ms);
                    adapter.notifyDataSetChanged();

                    lv_list.setSelection(ms.size() - 1 > 0 ? ms.size() - 1 : 0);

                } else {
                    Tools.showToast("没有更多数据了", context);
                }

                refreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        // 更新完后调用该方法结束刷新
                        refreshLayout.setRefreshing(false);
                        refreshLayout.setLoading(false);
                    }
                });
            }
        });

        //添加 + 号 面板
        final List<KeyboardAdd> gridData = new ArrayList<>();
        for (KeyboardAdd keyboardAdd : KeyboardAdd.values()) {
            gridData.add(keyboardAdd);
        }

        CommonAdapter grideAdapter = new CommonAdapter<KeyboardAdd>(context, R.layout.acty_chat_grid_item, gridData) {
            @Override
            protected void convert(ViewHolder vh, KeyboardAdd op, int position) {
                Glide.with(context)
                        .load(op.getDwRes())
                        .into((ImageView) vh.getView(R.id.iv_image));
                vh.setText(R.id.tv_name, op.getName());
            }
        };

        gridView.setAdapter(grideAdapter);
        grideAdapter.notifyDataSetChanged();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KeyboardAdd keyboardAdd = gridData.get(position);

                switch (keyboardAdd) {
                    case camera:         //拍照
                        Intent intent = new Intent(context, keyboardAdd.getJumpActy());
                        startActivityForResult(intent, 1001);
                        break;
                    case photo:         //图片
                        Intent intent1 = new Intent(context, keyboardAdd.getJumpActy());
                        startActivityForResult(intent1, IMAGE_PICKER);
                        break;
                    case video:          //视频
                        Bundle bundle = new Bundle();
                        bundle.putString("fromUser", Constant.Login_Name);
                        bundle.putString("toUser", Constant.chatToUser);
                        bundle.putInt("type", 0);
                        openActivity(keyboardAdd.getJumpActy(), bundle);
                        break;
                    case location:      //位置
                        Intent intent2 = new Intent(context, keyboardAdd.getJumpActy());
                        startActivityForResult(intent2, 1000);
                        break;
                }
            }
        });

        List<android.support.v4.app.Fragment> fragments = new ArrayList<>();

        List<String> emotionNames = new ArrayList<>();
        for (String emotion : EmotionUtil.emotions) {
            emotionNames.add(emotion);
            if (emotionNames.size() == 21) {

                EmojiFragment fragment = new EmojiFragment();
                fragment.setClickListener(new EmojiFragment.EmojiClickListener() {
                    @Override
                    public void click(String text) {
                        messageInput.setText(messageInput.getText().toString() + text);
                        messageInput.setSelection(messageInput.getText().toString().length());
                        btn_send_pic.setVisibility(View.GONE);
                        send_button.setVisibility(View.VISIBLE);
                    }
                });
                Bundle bl = new Bundle();
                bl.putString("list", JSON.toJSONString(emotionNames));
                fragment.setArguments(bl);
                fragments.add(fragment);
                emotionNames = new ArrayList<>();
            }
        }

        if (emotionNames.size() > 0) {
            EmojiFragment fragment = new EmojiFragment();
            fragment.setClickListener(new EmojiFragment.EmojiClickListener() {
                @Override
                public void click(String text) {
                    messageInput.setText(messageInput.getText().toString() + text);
                    messageInput.setSelection(messageInput.getText().toString().length());
                    btn_send_pic.setVisibility(View.GONE);
                    send_button.setVisibility(View.VISIBLE);
                }
            });
            Bundle bl = new Bundle();
            bl.putString("list", JSON.toJSONString(emotionNames));
            fragment.setArguments(bl);
            fragments.add(fragment);
        }

        String[] mTitles = new String[]{"未完成", "已完成"};

        ViewPageAdapter viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager(), fragments, mTitles);

        emjViewpager.setAdapter(viewPageAdapter);

        viewPageAdapter.notifyDataSetChanged();

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
            return !item.getFrom_user().equals(Constant.Login_Name);
        }

        @Override
        public void convert(ViewHolder holder, VFMessage vfMessage, int position) {

            RelativeLayout rl_audio = holder.getView(R.id.rl_audio);
            TextView tv_text = holder.getView(R.id.tv_text);
            LinearLayout ll_image = holder.getView(R.id.ll_image);

            holder.setText(R.id.tv_name, vfMessage.getFrom_user());

            Body_type type = vfMessage.getBodies().getType();

            if (type == Body_type.loc) {
                holder.getView(R.id.ll_locAddress).setVisibility(View.VISIBLE);
            } else {
                holder.getView(R.id.ll_locAddress).setVisibility(View.GONE);
            }

            switch (type) {
                case img:
                    ll_image.setVisibility(View.VISIBLE);
                    tv_text.setVisibility(View.GONE);
                    rl_audio.setVisibility(View.GONE);

                    String imageUrl = Constant.BaseUrl + "/" + vfMessage.getBodies().getFileRemotePath();
                    Glide.with(context)
                            .load(imageUrl)
                            .into((ImageView) holder.getView(R.id.iv_image));

                    break;
                case audio:
                    rl_audio.setVisibility(View.VISIBLE);
                    tv_text.setVisibility(View.GONE);
                    ll_image.setVisibility(View.GONE);
                    holder.setText(R.id.duration, vfMessage.getBodies().getDuration() + "''");
                    break;
                case txt:
                    tv_text.setVisibility(View.VISIBLE);
                    ll_image.setVisibility(View.GONE);
                    rl_audio.setVisibility(View.GONE);
                    tv_text.setText(vfMessage.getBodies().getMsg());
                    break;
                case loc:
                    TextView tv_title = holder.getView(R.id.tv_title);
                    TextView tv_title_details = holder.getView(R.id.tv_title_details);
                    tv_title.setText(vfMessage.getBodies().getLocationName());
                    tv_title_details.setText(vfMessage.getBodies().getDetailLocationName());

                    ll_image.setVisibility(View.VISIBLE);
                    tv_text.setVisibility(View.GONE);
                    rl_audio.setVisibility(View.GONE);

                    String locImageurl = Constant.BaseUrl + "/" + vfMessage.getBodies().getFileRemotePath();
                    Glide.with(context)
                            .load(locImageurl)
                            .into((ImageView) holder.getView(R.id.iv_image));

                    break;

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
            return item.getFrom_user().equals(Constant.Login_Name);
        }

        @Override
        public void convert(ViewHolder holder, VFMessage vfMessage, int position) {

            RelativeLayout rl_audio = holder.getView(R.id.rl_audio);
            TextView tv_text = holder.getView(R.id.tv_text);
            LinearLayout ll_image = holder.getView(R.id.ll_image);

            holder.setText(R.id.tv_name, vfMessage.getFrom_user());

            Body_type type = vfMessage.getBodies().getType();

            if (type == Body_type.loc) {
                holder.getView(R.id.ll_locAddress).setVisibility(View.VISIBLE);
            } else {
                holder.getView(R.id.ll_locAddress).setVisibility(View.GONE);
            }


            switch (type) {
                case img:
                    ll_image.setVisibility(View.VISIBLE);
                    tv_text.setVisibility(View.GONE);
                    rl_audio.setVisibility(View.GONE);

                    String imageUrl = Constant.BaseUrl + "/" + vfMessage.getBodies().getFileRemotePath();
                    Glide.with(context)
                            .load(imageUrl)
                            .into((ImageView) holder.getView(R.id.iv_image));

                    break;
                case audio:
                    rl_audio.setVisibility(View.VISIBLE);
                    tv_text.setVisibility(View.GONE);
                    ll_image.setVisibility(View.GONE);
                    holder.setText(R.id.duration, vfMessage.getBodies().getDuration() + "''");
                    break;
                case txt:
                    tv_text.setVisibility(View.VISIBLE);
                    ll_image.setVisibility(View.GONE);
                    rl_audio.setVisibility(View.GONE);
                    tv_text.setText(vfMessage.getBodies().getMsg());
                    break;
                case loc:

                    TextView tv_title = holder.getView(R.id.tv_title);
                    TextView tv_title_details = holder.getView(R.id.tv_title_details);
                    tv_title.setText(vfMessage.getBodies().getLocationName());
                    tv_title_details.setText(vfMessage.getBodies().getDetailLocationName());

                    ll_image.setVisibility(View.VISIBLE);
                    tv_text.setVisibility(View.GONE);
                    rl_audio.setVisibility(View.GONE);

                    String locImageurl = Constant.BaseUrl + "/" + vfMessage.getBodies().getFileRemotePath();
                    Glide.with(context)
                            .load(locImageurl)
                            .into((ImageView) holder.getView(R.id.iv_image));
                    break;

            }

        }

    }


    public void sendMessage(Body_type type) {
        try {

            String text = messageInput.getText().toString();

            if (TextUtils.isEmpty(text) && type == Body_type.txt) {
                return;
            }
            JSONObject obj = new JSONObject();

            obj.put("from_user", Constant.Login_Name);
            obj.put("to_user", toUser);
            obj.put("chat_type", "chat");

            JSONObject bodiesObj = new JSONObject();
            bodiesObj.put("type", type.toString());
            bodiesObj.put("msg", text);

            obj.put("bodies", bodiesObj);
            socket.emit("chat", obj, new Ack() {

                @Override
                public void call(Object... args) {

                    try {

                        if (args.length > 1) {      //给 服务器发回调 客户端收到消息了
                            Ack ack = (Ack) args[args.length - 1];
                            ack.call();
                        }

                        JSONObject obj = (JSONObject) args[0];
                        String bod = obj.getString("bodies");
                        Bodies bodies = JSON.parseObject(bod, Bodies.class);
                        obj.remove("bodies");

                        VFMessage msg = JSON.parseObject(obj.toString(), VFMessage.class);
                        msg.setBodies(bodies);

                        listData.add(msg);
                        messageDao.saveMessage(msg);
                        conversationDao.saveOrUpdateConversation(msg);

                        UiUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    adapter.notifyDataSetChanged();
                                    lv_list.setSelection(listData.size() - 1);
                                } catch (Exception e) {
                                }
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });


//            VFMessage message = JSON.parseObject(obj.toString(), VFMessage.class);
            messageInput.setText("");


        } catch (JSONException e) {

        }
    }


    @Override
    public void onResult(EventBusType data) {
        if (data.getTag().equals(EBConstant.CHATMESSAGE)) {
            VFMessage msg = (VFMessage) data.getE();
            if (!TextUtils.isEmpty(Constant.chatToUser) && Constant.chatToUser.equals(msg.getFrom_user())) {
                listData.add(msg);
                adapter.notifyDataSetChanged();
                lv_list.setSelection(listData.size() - 1 > 0 ? listData.size() - 1 : 0);
            } else {
                if (!TextUtils.isEmpty(Constant.chatToUser)) {
                    String msgTxt = msg.getBodies().getType().equals(Body_type.txt) ? msg.getBodies().getMsg() : "[图片]";
                    Tools.showToast(msgTxt, context);
                }
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            onUserInteraction();
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }


    @OnClick({R.id.send_button, R.id.btn_send_pic})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_button:
                sendMessage(Body_type.txt);
                break;
            case R.id.btn_send_pic:
                popEmoji.setChecked(false);
                popVoice.setChecked(false);
                include_voice_view.setVisibility(View.GONE);
                include_more.setVisibility(include_more.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
        }

    }

    @Override
    public boolean beforeBack() {
        chatToUser = "";
        KeyboardUtils.hideSoftInput(ChatActy.this);
        EventBus.getDefault().post(new EventBusType(EBConstant.CONVERSATIONS));
        return super.beforeBack();
    }


    public void sendImage(final ImageItem im) {
        try {
            JSONObject obj = new JSONObject();

            obj.put("from_user", Constant.Login_Name);
            obj.put("to_user", toUser);
            obj.put("chat_type", "chat");

            JSONObject bodiesObj = new JSONObject();
            bodiesObj.put("type", "img");
            byte[] fileByte = File2byte(im.path);
            bodiesObj.put("fileData", fileByte);
            bodiesObj.put("fileName", im.name);

            JSONObject sizeObj = new JSONObject();
            sizeObj.put("width", im.width);
            sizeObj.put("height", im.height);
            bodiesObj.put("size", sizeObj);

            obj.put("bodies", bodiesObj);

            socket.emit("chat", obj, new Ack() {

                @Override
                public void call(Object... args) {

                    try {
                        baseDialog.dismiss();
                        if (args.length > 1) {      //给 服务器发回调 客户端收到消息了
                            Ack ack = (Ack) args[args.length - 1];
                            ack.call();
                        }

                        JSONObject obj = (JSONObject) args[0];
                        String bod = obj.getString("bodies");
                        Bodies bodies = JSON.parseObject(bod, Bodies.class);
                        obj.remove("bodies");

                        VFMessage msg = JSON.parseObject(obj.toString(), VFMessage.class);
                        msg.setBodies(bodies);

                        listData.add(msg);
                        messageDao.saveMessage(msg);
                        conversationDao.saveOrUpdateConversation(msg);

                        UiUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                lv_list.setSelection(listData.size() - 1 > 0 ? listData.size() - 1 : 0);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //发送地理位置
    public void sendLocation(double lat, double lon, String location, String detail) {
        try {
            JSONObject obj = new JSONObject();

            obj.put("from_user", Constant.Login_Name);
            obj.put("to_user", toUser);
            obj.put("chat_type", "chat");

            JSONObject bodiesObj = new JSONObject();
            bodiesObj.put("type", "loc");
            bodiesObj.put("longitude", lon);
            bodiesObj.put("latitude", lat);
            bodiesObj.put("locationName", location);
            bodiesObj.put("detailLocationName", detail);
            obj.put("bodies", bodiesObj);


            socket.emit("chat", obj, new Ack() {

                @Override
                public void call(Object... args) {

                    try {
                        baseDialog.dismiss();
                        if (args.length > 1) {      //给 服务器发回调 客户端收到消息了
                            Ack ack = (Ack) args[args.length - 1];
                            ack.call();
                        }

                        JSONObject obj = (JSONObject) args[0];
                        String bod = obj.getString("bodies");
                        Bodies bodies = JSON.parseObject(bod, Bodies.class);
                        obj.remove("bodies");

                        VFMessage msg = JSON.parseObject(obj.toString(), VFMessage.class);
                        msg.setBodies(bodies);

                        listData.add(msg);
                        messageDao.saveMessage(msg);
                        conversationDao.saveOrUpdateConversation(msg);

                        UiUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                lv_list.setSelection(listData.size() - 1 > 0 ? listData.size() - 1 : 0);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (resultCode == ImagePicker.RESULT_CODE_ITEMS && requestCode == IMAGE_PICKER) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);

                for (final ImageItem im : images) {
                    sendImage(im);
                }

            } else if (resultCode == 101) {     // 地理位置


                double lat = data.getDoubleExtra("lat", 0.0);
                double lon = data.getDoubleExtra("lon", 0.0);
                String location = data.getStringExtra("location");
                String detail = data.getStringExtra("detailLocation");

                sendLocation(lat, lon, location, detail);

            } else if (resultCode == 102) {     // 照片

                ImageItem im = new ImageItem();
                im.name = data.getStringExtra("imageName");
                im.width = data.getIntExtra("imageWidth", 0);
                im.height = data.getIntExtra("imageHeight", 0);
                im.path = data.getStringExtra("imagePath");
                sendImage(im);
            }
        }
    }


    public static byte[] File2byte(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    public static void byte2File(byte[] buf, String filePath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory()) {
                dir.mkdirs();
            }
            file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

}
