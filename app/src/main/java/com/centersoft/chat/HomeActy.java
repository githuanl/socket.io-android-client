package com.centersoft.chat;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.centersoft.base.BaseActivity;
import com.centersoft.base.ChatApplication;
import com.centersoft.db.DBManager;
import com.centersoft.db.dao.ConversationDao;
import com.centersoft.db.dao.VFMessageDao;
import com.centersoft.effect.VFPageAdapter;
import com.centersoft.effect.VFViewPager;
import com.centersoft.entity.Bodies;
import com.centersoft.entity.EventBusType;
import com.centersoft.entity.VFMessage;
import com.centersoft.fragment.ContactFragment;
import com.centersoft.fragment.ConversationListFragment;
import com.centersoft.fragment.MeFragment;
import com.centersoft.service.VFChatService;
import com.centersoft.util.AppManager;
import com.centersoft.util.Constant;
import com.centersoft.util.EBConstant;
import com.centersoft.util.MyLog;
import com.centersoft.util.ScreenListener;
import com.centersoft.util.Tools;
import com.centersoft.util.UiUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import io.socket.client.Ack;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

// 首页
public class HomeActy extends BaseActivity {


    Socket socket;

    @BindView(R.id.viewpager)
    VFViewPager viewpager;

    @BindView(R.id.ll_bottom)
    LinearLayout ll_bottom;

    @BindView(R.id.mTabLayout)
    TabLayout mTabLayout;

    List<Fragment> listFragment;
    String[] mTitles = {"消息", "联系人", "我"};


    int[] draWbles = {R.drawable.home_tab_message, R.drawable.home_tab_txl, R.drawable.home_tab_me};
    int[] draWbleSelects = {R.drawable.home_tab_messages_select, R.drawable.home_tab_txl_select, R.drawable.home_tab_me_select};

    @BindColor(R.color.home_tab_text)
    int home_tab_text;

    @BindColor(R.color.colorPrimaryDark)
    int colorPrimaryDark;

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
        toobarTitle.setText(mTitles[0]);
    }

    VFMessageDao dao;
    ConversationDao conversationDao;

    @Override
    protected void initData() {


        dao = new VFMessageDao();
        conversationDao = new ConversationDao();

        listFragment = new ArrayList<Fragment>();

        listFragment.add(new ConversationListFragment());
        listFragment.add(new ContactFragment());
        listFragment.add(new MeFragment());

        VFPageAdapter adapter = new VFPageAdapter(getSupportFragmentManager(), listFragment, mTitles);
        viewpager.setAdapter(adapter);
        viewpager.setOffscreenPageLimit(listFragment.size());
        adapter.notifyDataSetChanged();

        initBottomTab();

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                resetBottomTab(position);
                viewpager.setCurrentItem(position, false);
                toobarTitle.setText(mTitles[position]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        resetBottomTab(0);

//      mTabLayout.getTabAt(0).select();

        initSocketData();
    }

    boolean onScreenOff = false;

    ScreenListener l;
    private NotificationManager nm;
    private PowerManager.WakeLock m_wakeLockObj = null;
    private final int ID_LED = 19871103;

    private void initSocketData() {

        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        ChatApplication.closeSocket();

        socket = ChatApplication.getSocket();

        socket.on(Socket.EVENT_CONNECT, onConnect)
                .on("chat", onChat)
                .on(Socket.EVENT_DISCONNECT, onDisconnect)
                .on(Socket.EVENT_CONNECT_ERROR, onConnectError)
                .on(Socket.EVENT_CONNECT_TIMEOUT, onConnectTimeoutError)
                .on("onLine", onLine)
                .on("GroupChat", GroupChat)
                .on("notification", notification)
                .on("offLine", offLine);

        // 视频通话请求
        socket.on("videoChat", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                JSONObject dataObject = (JSONObject) args[0];

                try {
                    String fromUser = dataObject.getString("from_user");
                    String room = dataObject.getString("room");

                    Bundle bundle = new Bundle();
                    bundle.putString("fromUser", fromUser);
                    bundle.putString("toUser", Constant.Login_Name);
                    bundle.putString("room", room);
                    bundle.putInt("type", 1);
                    openActivity(VideoChatActivity.class, bundle);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        socket.connect();

        if (l == null) {
            l = new ScreenListener(this);
            l.begin(new ScreenListener.ScreenStateListener() {

                @Override
                public void onUserPresent() {
                    onScreenOff = false;
                    MyLog.i("onUserPresent", "onUserPresent");
                    socket = ChatApplication.getSocket().open();
                }

                @Override
                public void onScreenOn() {
                    onScreenOff = false;
                    MyLog.i("onScreenOn", "onScreenOn");
                    socket = ChatApplication.getSocket().open();
                }

                @Override
                public void onScreenOff() {
                    onScreenOff = true;
                    MyLog.i("onScreenOff", "onScreenOff");
                    Intent intent = new Intent(context, VFChatService.class);
                    startService(intent);
                }
            });
        }

//        Intent intent = new Intent(this, VFChatService.class);
//        startService(intent);
    }


    //聊天
    private Emitter.Listener onChat = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            try {

                Ack ack = (Ack) args[1];
                ack.call(1);

                JSONObject obj = (JSONObject) args[0];
                String bod = obj.getString("bodies");
                Bodies bodies = JSON.parseObject(bod, Bodies.class);
                obj.remove("bodies");

                VFMessage msg = JSON.parseObject(obj.toString(), VFMessage.class);
                msg.setBodies(bodies);

                if (!msg.getFrom_user().equals(Constant.Login_Name)) {

                    if (!isActive) { //进入后台时 显示通知
                        Notification noti = new Notification.Builder(context)
                                .setContentTitle(msg.getFrom_user())
                                .setContentText(msg.getBodies().getMsg())
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                                .setDefaults(Notification.DEFAULT_SOUND)
                                .build();
                        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        nm.notify(0, noti);

                        ChatApplication.getHandler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                nm.cancel(0);
                            }
                        }, 1500);
                    }

                    if (onScreenOff) {   //锁屏后处理
                        /*打开指示灯*/
                        Notification notification = new Notification();
                        notification.ledARGB = 0x00FF00;  //这里是颜色，我们可以尝试改变，理论上0xFF0000是红色，0x00FF00是绿色
                        notification.ledOnMS = 1000;
                        notification.ledOffMS = 1000;
                        notification.flags = Notification.FLAG_SHOW_LIGHTS;
                        nm.notify(ID_LED, notification);

	                     /*点亮屏幕*/
                        if (m_wakeLockObj == null) {
                            PowerManager pm = (PowerManager) context.getSystemService(context.POWER_SERVICE);
                            m_wakeLockObj = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                                    | PowerManager.ACQUIRE_CAUSES_WAKEUP
                                    | PowerManager.ON_AFTER_RELEASE, "VF");

                        }
                        m_wakeLockObj.acquire(1200);

                    }

                    MediaPlayer player = MediaPlayer.create(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    player.setLooping(false);    //设置是否循环播放
                    player.setVolume(1.0f, 1.0f);
                    player.start();

                    dao.saveMessage(msg);

                    int unreadnum = conversationDao.saveOrUpdateConversation(msg);
                    msg.setUnreadnum(unreadnum);

                    EventBus.getDefault().post(new EventBusType<VFMessage>(EBConstant.CHATMESSAGE, msg));


                    if (TextUtils.isEmpty(Constant.chatToUser) || !Constant.chatToUser.equals(msg.getFrom_user())) {         //不是聊天界面的时候添加未读数
                        final int countUnreadNum = conversationDao.getAllConversationUnreadNum(); //所有未读数
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (msgtv != null && countUnreadNum > 0) {
                                    msgtv.setText(countUnreadNum + "");
                                }
                                msgtv.setVisibility(countUnreadNum > 0 ? View.VISIBLE : View.GONE);
                            }
                        });
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 实现消息回调接口
     */
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Tools.showToast("连接成功", context);
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            socket.open();
            Tools.showToast("连接关闭", context);
        }
    };


    //群聊
    private Emitter.Listener GroupChat = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            org.json.JSONObject data = (org.json.JSONObject) args[0];

            try {
                Tools.showToast(data.optString("say"), context);
            } catch (Exception e) {

            }
        }
    };

    //上线
    private Emitter.Listener onLine = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            org.json.JSONObject data = (org.json.JSONObject) args[0];
            String name = data.optString("user");
            EventBus.getDefault().post(new EventBusType(EBConstant.MESSAGEONLINE, name));
        }
    };


    //notification
    private Emitter.Listener notification = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            if (!isActive) {
                Notification noti = new Notification.Builder(context)
                        .setContentTitle("通知")
                        .setContentText(args[0].toString())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .build();
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(0, noti);
            } else {

                MediaPlayer player = MediaPlayer.create(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.setLooping(false);    //设置是否循环播放
                player.setVolume(1.0f, 1.0f);
                player.start();

                UiUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Tools.showToast(args[0].toString(), context);
                    }
                });
            }
        }
    };

    //下线
    private Emitter.Listener offLine = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            org.json.JSONObject data = (org.json.JSONObject) args[0];
            EventBus.getDefault().post(new EventBusType(EBConstant.MESSAGEOFFLINE, data.optString("user")));
        }
    };


    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            socket.open();
        }
    };

    private Emitter.Listener onConnectTimeoutError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            socket.open();
        }
    };

    TextView msgtv;

    /**
     * 调整tab
     */
    private void resetBottomTab(int positon) {

        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tabIndex = mTabLayout.getTabAt(i);
            View view = tabIndex.getCustomView();
            TextView tv = (TextView) view.findViewById(R.id.title);
            ImageView imageView = (ImageView) view.findViewById(R.id.img);
            tv.setTextColor(home_tab_text);
            imageView.setImageResource(draWbles[i]);
        }

        TabLayout.Tab tab = mTabLayout.getTabAt(positon);
        int position = tab.getPosition();
        View view = tab.getCustomView();
        TextView tv = (TextView) view.findViewById(R.id.title);
        ImageView imageView = (ImageView) view.findViewById(R.id.img);
        tv.setTextColor(colorPrimaryDark);
        imageView.setImageResource(draWbleSelects[position]);
    }

    /**
     * 初始化tab
     */
    private void initBottomTab() {
        int i = 0;
        for (String str : mTitles) {
            View view = LayoutInflater.from(this).inflate(R.layout.acty_home_tab, null);
            TextView tv = (TextView) view.findViewById(R.id.title);

            if (i == 0) {
                TextView msg = (TextView) view.findViewById(R.id.tv_message);
                msgtv = msg;
            }

            ImageView imageView = (ImageView) view.findViewById(R.id.img);
            tv.setText(str);
            tv.setTextColor(home_tab_text);
            imageView.setImageResource(draWbles[i]);
            mTabLayout.addTab(mTabLayout.newTab().setCustomView(view));
            i++;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (TextUtils.isEmpty(Constant.auth_Token)) {       //说明被杀死了
            Constant.auth_Token = SPUtils.getInstance().getString("auth_token");
            Constant.Login_Name = SPUtils.getInstance().getString("userName");
            initSocketData();
            return;
        }

        socket = ChatApplication.getSocket().open();

        int countUnreadNum = conversationDao.getAllConversationUnreadNum(); //所有未读数
        if (msgtv != null && countUnreadNum > 0) {
            msgtv.setText(countUnreadNum + "");
        }
        msgtv.setVisibility(countUnreadNum > 0 ? View.VISIBLE : View.GONE);

    }

    private long mExitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Tools.showToast("再按一次退出应用", context);
                mExitTime = System.currentTimeMillis();
            } else {
                DBManager.getInstance().closeDB();
                AppManager.getInstance().AppExit(context);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (l != null) {
            l.unregisterListener();
        }

        stopService(new Intent(this, VFChatService.class));

        MyLog.i(Tag, "homedestory===");

        socket.disconnect();
        socket.off(Socket.EVENT_CONNECT, onConnect);// 连接成功
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect);// 断开连接
        socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);// 连接异常
        socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectTimeoutError);// 连接超时
        socket.off("chat", onChat);
        socket.off("notification", notification);
        socket.off("GroupChat", GroupChat);
        socket.off("onLine", onLine);
        socket.off("offLine", offLine);
        socket.off("videoChat");



        socket = null;

    }
}
