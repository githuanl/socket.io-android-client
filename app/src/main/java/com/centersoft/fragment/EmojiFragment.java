package com.centersoft.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.alibaba.fastjson.JSON;
import com.centersoft.base.BaseFragment;
import com.centersoft.chat.R;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

import java.util.List;

import butterknife.BindView;

/**
 * Created by liudong on 2017/7/18.
 */

public class EmojiFragment extends BaseFragment {

    @Override
    public int initViews() {
        return R.layout.acty_chat_emj_grid;
    }

    @BindView(R.id.gridView)
    GridView gridView;

    public interface EmojiClickListener {
        void click(String text);
    }

    EmojiClickListener listener;

    public void setClickListener(EmojiClickListener listener) {
        this.listener = listener;
    }

    List<String> list;

    @Override
    protected void initData() {
        Bundle bl = getArguments();

        list = JSON.parseArray(bl.getString("list"), String.class);

        CommonAdapter gv = new CommonAdapter<String>(context, R.layout.acty_chat_emj_grid_item, list) {
            @Override
            protected void convert(ViewHolder vh, String str, int position) {
                vh.setText(R.id.tv_text, str);
            }
        };
        gridView.setAdapter(gv);
        gv.notifyDataSetChanged();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = list.get(position);
                if (listener != null) {
                    listener.click(str);
                }
            }
        });

    }
}
