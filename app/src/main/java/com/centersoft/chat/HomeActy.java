package com.centersoft.chat;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
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
import com.centersoft.util.Tools;

import org.greenrobot.eventbus.EventBus;
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


    private void initSocketData() {

        ChatApplication.closeSocket();

        socket = ChatApplication.getSocket();

        socket.on(Socket.EVENT_CONNECT, onConnect)
                .on("chat", onChat)
                .on(Socket.EVENT_DISCONNECT, onDisconnect)
                .on(Socket.EVENT_CONNECT_ERROR, onConnectError)
                .on(Socket.EVENT_CONNECT_TIMEOUT, onConnectTimeoutError)
                .on("onLine", onLine)
                .on("offLine", offLine);
        socket.connect();


        Intent intent = new Intent(this, VFChatService.class);
        startService(intent);


    }

    /**
     * 实现消息回调接口
     */
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Tools.showToast("连接成功", context);
        }
    };

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

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (!Constant.exit && !socket.connected()) { //重新连接
                socket.open();
                return;
            }
            Tools.showToast("连接关闭", context);
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
            if (!Constant.exit && !socket.connected()) { //重新连接
                socket.open();
                return;
            }
            Tools.showToast("连接出错", context);

        }
    };

    private Emitter.Listener onConnectTimeoutError = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Tools.showToast("连接超时", context);
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

        if (!socket.connected()) { //示连接则重新连接
            socket.open();
        }

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

        stopService(new Intent(this, VFChatService.class));

        Constant.exit = true;

        MyLog.i(Tag, "homedestory===");

        socket.disconnect();

        socket.off(Socket.EVENT_CONNECT, onConnect);// 连接成功
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect);// 断开连接
        socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);// 连接异常
        socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectTimeoutError);// 连接超时
        socket.off("chat", onChat);
        socket.off("onLine", onLine);
        socket.off("offLine", offLine);
        socket = null;

    }
}
