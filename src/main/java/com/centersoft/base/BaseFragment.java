package com.centersoft.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.centersoft.entity.EventBusType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * fragment基类
 */
public abstract class BaseFragment extends Fragment {

    protected String Tag = BaseFragment.class.getSimpleName();
    protected Unbinder unbinder;
    protected boolean isVisible = false; // 是否可见
    protected Activity context;
    protected Map<String,String> baseReqMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);//订阅
        context = getActivity();
        baseReqMap = new HashMap<>();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);//取消订阅
        unbinder.unbind();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)                //在ui线程执行
    public void onResult(EventBusType data) {

    }


    // 处理fragment的布局
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = View.inflate(context, initViews(), null);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    // 依附的activity创建完成
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {  //可见时加载数据
            isVisible = true;
            reqData();
        } else {    //不可见时不执行操作
            isVisible = false;
        }
    }

    // 加载布局
    public abstract int initViews();

    // 初始化
    protected void initData() {
    }

    //请求网络数据
    protected void reqData() {
    }


}
