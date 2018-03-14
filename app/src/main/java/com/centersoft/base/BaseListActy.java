package com.centersoft.base;

import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;

import com.centersoft.chat.R;
import com.centersoft.effect.VFSwipeRefreshLayout;
import com.zhy.adapter.abslistview.CommonAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by liudong on 16/8/12.
 */

public abstract class BaseListActy<T> extends BaseActivity {

    @BindView(R.id.refresh_view)
    protected VFSwipeRefreshLayout refreshLayout;

    @BindView(R.id.lv_list)
    protected ListView lv_list;

    protected List<T> listData = new ArrayList();

    protected int offset = 0;                    //动态加载数据 开始
    protected int limit = 20;                   //动态加载数据 一次加载20条数据

    protected Boolean loadMoreData = true;      // 是否加载更多
    protected Boolean isRefreshData = true;     // 下拉刷新

    //公用数据适配器
    protected CommonAdapter comAdapter;


    @Override
    protected void initData() {
        refreshLayout.setColorSchemeResources(R.color.colorPrimaryDark, R.color.orange_500, R.color.green_500,
                R.color.colorPrimaryDark);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                offset = 0;
                isRefreshData = true;
                loadMoreData = true;
                reqData();
            }
        });

        // 加载监听器
        refreshLayout.setOnLoadListener(new VFSwipeRefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                if (loadMoreData) {
                    offset += limit;
                    reqData();
                } else {
                    refreshLayout.setLoading(false);
                }
            }
        });
        startRefresh();
    }


    protected void reqData() {

    }

    ;

    /**
     * @param result
     * @throws
     * @Title 显示数据
     * @Description TODO
     * @author LH
     * @Createdate 2015年7月7日 下午11:38:32
     */
    public void showViewData(String result, Class cla) {
        List ls = parseArray(result, cla);
        int size = ls.size();

        //停止刷新
        endRefresh();

        if (size == 0 && offset == 0) {                //第一次加载数据没有数据
            listData.clear();
            return;
        }
        if (size != limit) {
            loadMoreData = false;
        }
        if (offset == 0 && isRefreshData) {
            listData.clear();
        }

        listData.addAll(ls);

    }

    //错误处理
    protected void listError(int e) {
        endRefresh();
        offset -= limit;
    }

    //开始刷新
    protected void startRefresh() {
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setLoading(false);
                refreshLayout.setRefreshing(true);
                offset = 0;
                isRefreshData = true;
                loadMoreData = true;
                reqData();
            }
        });
    }


    //结束刷新
    protected void endRefresh() {

        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                // 更新完后调用该方法结束刷新
                refreshLayout.setRefreshing(false);
                refreshLayout.setLoading(false);
            }
        });
    }

}
