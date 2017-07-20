package com.centersoft.chat;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.centersoft.base.BaseActivity;
import com.centersoft.base.ChatApplication;
import com.centersoft.dao.VFMessageDao;
import com.centersoft.effect.VFPageAdapter;
import com.centersoft.effect.VFViewPager;
import com.centersoft.entity.Bodies;
import com.centersoft.entity.EventBusType;
import com.centersoft.entity.VFMessage;
import com.centersoft.fragment.ContactFragment;
import com.centersoft.fragment.ConversationListFragment;
import com.centersoft.fragment.MeFragment;
import com.centersoft.util.AppManager;
import com.centersoft.util.Constant;
import com.centersoft.util.EBConstant;
import com.centersoft.util.MyLog;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
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

    @Override
    protected void initData() {

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

    VFMessageDao vfMessageDao;

    private void initSocketData() {

        vfMessageDao = ChatApplication.getDaoInstant().getVFMessageDao();
        socket = ChatApplication.getSocket();

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(HomeActy.this, "连接成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).on("chat", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                try {

                    JSONObject obj = (JSONObject) args[0];
                    String bod = obj.getString("bodies");
                    Bodies bodies = JSON.parseObject(bod, Bodies.class);
                    obj.remove("bodies");

                    VFMessage msg = JSON.parseObject(obj.toString(), VFMessage.class);
                    msg.setBodies(bodies);


                    if (!msg.getFrom().equals(Constant.Login_Name)) {

                        EventBus.getDefault().post(new EventBusType(EBConstant.CHATMESSAGE, msg));

                        vfMessageDao.save(msg);
                        MyLog.i(Tag, JSON.toJSONString(vfMessageDao.loadAll()));

                        MediaPlayer player = MediaPlayer.create(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        player.setLooping(false);    //设置是否循环播放
                        player.setVolume(1.0f, 1.0f);
                        player.start();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

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
                        Toast.makeText(HomeActy.this, "连接失败", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(HomeActy.this, "重新连接", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });
        socket.connect();
    }


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
            ImageView imageView = (ImageView) view.findViewById(R.id.img);
            tv.setText(str);
            tv.setTextColor(home_tab_text);
            imageView.setImageResource(draWbles[i]);
            mTabLayout.addTab(mTabLayout.newTab().setCustomView(view));
            i++;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.close();
        AppManager.getInstance().AppExit(context);
    }
}
