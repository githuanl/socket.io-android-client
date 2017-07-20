package com.centersoft.fragment;

import com.centersoft.base.BaseListFragment;
import com.centersoft.chat.R;
import com.centersoft.entity.EventBusType;
import com.centersoft.entity.VFMessage;
import com.centersoft.util.EBConstant;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

/**
 * Created by liudong on 2017/7/18.
 */

public class ConversationListFragment extends BaseListFragment<VFMessage> {


    @Override
    public int initViews() {
        return R.layout.fg_conversation;
    }


    @Override
    protected void initData() {
        super.initData();
        refreshLayout.setEnabled(false);
        comAdapter = new CommonAdapter<VFMessage>(context, R.layout.fg_history_list_item, listData) {
            @Override
            protected void convert(ViewHolder viewHolder, VFMessage item, int position) {

            }
        };
        lv_list.setAdapter(comAdapter);
    }

    @Override
    public void onResult(EventBusType data) {
        if (data.getTag().equals(EBConstant.CHATMESSAGE)) {
            listData.add((VFMessage) data.getE());
            comAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void reqData() {

    }

}
