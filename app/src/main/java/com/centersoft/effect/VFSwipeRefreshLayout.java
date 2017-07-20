package com.centersoft.effect;

/**
 * Created by zs on 2016/10/15.
 */

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ListView;

import com.centersoft.chat.R;
import com.centersoft.util.UiUtils;


/**
 * 继承自SwipeRefreshLayout,从而实现滑动到底部时上拉加载更多的功能.
 *
 * @author lh
 */
public class VFSwipeRefreshLayout extends SwipeRefreshLayout {
    private final int mScaledTouchSlop;
    private final View mFooterView;
    private ListView mListView;
    private OnLoadListener mOnLoadListener;
    /**
     * 正在加载状态
     */
    private boolean isLoading;

    public VFSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 填充底部加载布局
        mFooterView = View.inflate(context, R.layout.listview_footer, null);
        // 表示控件移动的最小距离，手移动的距离大于这个距离才能拖动控件
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 获取ListView,设置ListView的布局位置
        if (mListView == null) {
            // 判断容器有多少个孩子
            if (getChildCount() > 0) {
                // 判断第一个孩子是不是ListView
                if (getChildAt(0) instanceof ListView) {
                    // 创建ListView对象
                    mListView = (ListView) getChildAt(0);
                    // 设置ListView的滑动监听
                    setListViewOnScroll();
                }
            }
        }
    }

    /**
     * 在分发事件的时候处理子控件的触摸事件
     *
     * @param ev
     * @return
     */
    private float mDownY, mUpY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 移动的起点
                mDownY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                // 移动的终点
                mUpY = getY();
                break;
            case MotionEvent.ACTION_MOVE:
                // 移动过程中判断时候能下拉加载更多
                if (mUpY > 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            UiUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (canLoadMore()) {
                                        // 加载数据
                                        loadData();
                                    }
                                }
                            });
                        }
                    }).start();
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }


    /**
     * 判断是否满足加载更多条件
     *
     * @return
     */
    private boolean canLoadMore() {
        // 1. 是上拉状态
        boolean condition1 = (mDownY - mUpY) >= mScaledTouchSlop;
//        if (condition1) {
//            MyLog.d("MySwipeRefreshLayout-->", "是上拉状态");
//        }

        // 2. 当前页面可见的item是最后一个条目
        boolean condition2 = false;
        if (mListView != null && mListView.getAdapter() != null) {
            condition2 = mListView.getLastVisiblePosition() == (mListView.getAdapter().getCount() - 1);
        }

//        if (condition2) {
//            MyLog.d("MySwipeRefreshLayout-->", "是最后一个条目");
//        }
        // 3. 正在加载状态
        boolean condition3 = !isLoading;
//        if (condition3) {
//            MyLog.d("MySwipeRefreshLayout-->", "不是正在加载状态");
//        }
        return condition1 && condition2 && condition3;
    }

    /**
     * 处理加载数据的逻辑
     */
    private void loadData() {
//        MyLog.i("MySwipeRefreshLayout-->", "加载数据...");
        if (mOnLoadListener != null) {
            // 设置加载状态，让布局显示出来
            setLoading(true);
            mOnLoadListener.onLoad();
        }

    }

    /**
     * 设置加载状态，是否加载传入boolean值进行判断
     *
     * @param loading
     */
    public void setLoading(boolean loading) {
        // 修改当前的状态
        isLoading = loading;
        if (isLoading) {
            if (mListView != null && mListView.getAdapter().getCount() > 0) {
                // 显示布局
                mListView.addFooterView(mFooterView);
                mListView.setSelection(mListView.getBottom());
            }
        } else {
            if (mListView != null && mFooterView != null) {
                // 隐藏布局
                mListView.removeFooterView(mFooterView);
            }
            // 重置滑动的坐标
            mDownY = 0;
            mUpY = 0;
        }
    }

    /**
     * 设置ListView的滑动监听
     */
    private void setListViewOnScroll() {

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                int lastPostion = mListView.getLastVisiblePosition();// 获取listview
                if (lastPostion == (mListView.getAdapter().getCount() - 1)) {
                    // 移动过程中判断时候能下拉加载更多
                    if (canLoadMore()) {
                        // 加载数据
                        loadData();
                    }
                }

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                int lastPostion = mListView.getLastVisiblePosition();
//                if (mListView.getAdapter() != null && lastPostion == (mListView.getAdapter().getCount() - 1)) {
//                    new Thread(
//                            new Runnable() {
//                                @Override
//                                public void run() {
//                                    UiUtils.runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            if (canLoadMore()) {
//                                                // 加载数据
//                                                loadData();
//                                            }
//                                        }
//                                    });
//                                }
//                            }
//
//                    ).start();
//                }
            }
        });
    }


    /**
     * 上拉加载的接口回调
     */

    public interface OnLoadListener {
        void onLoad();
    }

    public void setOnLoadListener(OnLoadListener listener) {
        this.mOnLoadListener = listener;
    }
}
