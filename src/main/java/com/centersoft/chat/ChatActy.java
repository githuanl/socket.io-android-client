package com.centersoft.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.SPUtils;
import com.bumptech.glide.Glide;
import com.centersoft.ICallBack.IDataCallBack;
import com.centersoft.base.BaseActivity;
import com.centersoft.base.ChatApplication;
import com.centersoft.db.dao.ConversationDao;
import com.centersoft.db.dao.VFMessageDao;
import com.centersoft.effect.VFSwipeRefreshLayout;
import com.centersoft.entity.Bodies;
import com.centersoft.entity.EventBusType;
import com.centersoft.entity.User;
import com.centersoft.entity.VFMessage;
import com.centersoft.enums.Body_type;
import com.centersoft.enums.VFCode;
import com.centersoft.util.Constant;
import com.centersoft.util.EBConstant;
import com.centersoft.util.GlideImageLoader;
import com.centersoft.util.KeyboardStatusDetector;
import com.centersoft.util.MyLog;
import com.centersoft.util.NetTool;
import com.centersoft.util.Tools;
import com.centersoft.util.UiUtils;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.zhy.adapter.abslistview.MultiItemTypeAdapter;
import com.zhy.adapter.abslistview.ViewHolder;
import com.zhy.adapter.abslistview.base.ItemViewDelegate;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import io.socket.client.Ack;
import io.socket.client.Socket;

import static com.centersoft.util.Constant.chatToUser;

//聊天界面
public class ChatActy extends BaseActivity {

    private static final int IMAGE_PICKER = 2;

    @BindView(R.id.refresh_view)
    protected VFSwipeRefreshLayout refreshLayout;

    @BindView(R.id.lv_list)
    protected ListView lv_list;

    protected List<VFMessage> listData = new ArrayList();

    protected int offset = 0;                    //动态加载数据 开始
    protected int limit = 20;                   //动态加载数据 一次加载20条数据

    protected Boolean loadMoreData = true;      // 是否加载更多

    MultiItemTypeAdapter adapter;

    @BindView(R.id.message_input)
    EditText messageInput;

    Socket socket;

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

    @BindView(R.id.tv_message)
    TextView tv_message;    //新消息

    @BindView(R.id.send_button)
    Button send_button;

    String toUser = "";
    User user;

    VFMessageDao messageDao;
    ConversationDao conversationDao;

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

        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        new KeyboardStatusDetector()
                .registerView(rootView)  //or register to an activity
                .setVisibilityListener(new KeyboardStatusDetector.KeyboardVisibilityListener() {
                    @Override
                    public void onVisibilityChanged(boolean keyboardVisible) {
                        if (keyboardVisible) {
                            lv_list.setSelection(listData.size() - 1 > 0 ? listData.size() - 1 : 0);
                        }
                    }
                });

        messageInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage(Body_type.txt, "");
                    return true;
                }
                return false;
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
                holder.setText(R.id.tv_name, vfMessage.getFrom_user())
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
            return item.getFrom_user().equals(Constant.Login_Name);
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
                holder.setText(R.id.tv_name, vfMessage.getFrom_user())
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

            obj.put("from_user", Constant.Login_Name);
            obj.put("to_user", toUser);
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
                    tv_message.setText(msg.getFrom_user() + ":" + msgTxt);
                    ObjectAnimator ob = ObjectAnimator.ofFloat(tv_message, "translationY", 0, tv_message.getHeight()).setDuration(500);
                    ob.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            SystemClock.sleep(2000);
                            ObjectAnimator.ofFloat(tv_message, "translationY", tv_message.getHeight(), 0).setDuration(500).start();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    ob.start();

                }
            }
        }
//        else if (data.getTag().equals(EBConstant.MESSAGEONLINE) || data.getTag().equals(EBConstant.MESSAGEOFFLINE)) {
//            if (data.getE().toString().equals(toUser)) {
//                toobarTitle.setText((data.getTag().equals(EBConstant.MESSAGEONLINE)) && data.getE().equals(toUser) ? toUser + "[在线]" : toUser + "[离线]");
//            }
//        }
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
                sendMessage(Body_type.txt, "");
                break;
            case R.id.btn_send_pic:
                Intent intent = new Intent(this, ImageGridActivity.class);
                startActivityForResult(intent, IMAGE_PICKER);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == IMAGE_PICKER) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);

                for (final ImageItem im : images) {

                    baseDialog.show();
                    baseReqMap.clear();

                    Map<String, String> map = new HashMap<>();
                    baseReqMap.put("image", im.path);
                    map.put("file_submit", JSON.toJSONString(baseReqMap));
                    map.put("auth_token", SPUtils.getInstance().getString("auth_token"));
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

                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        socket.open();
    }

}
