package com.centersoft.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.centersoft.base.BaseActivity;
import com.centersoft.base.BaseListFragment;
import com.centersoft.chat.ChatActy;
import com.centersoft.chat.R;
import com.centersoft.db.dao.ConversationDao;
import com.centersoft.effect.GlideCircleTransform;
import com.centersoft.entity.EventBusType;
import com.centersoft.entity.VFMessage;
import com.centersoft.enums.Body_type;
import com.centersoft.util.Constant;
import com.centersoft.util.EBConstant;
import com.centersoft.util.Tools;
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

    ConversationDao conversationDao;

    @Override
    protected void initData() {
        super.initData();
        conversationDao = new ConversationDao();
        refreshLayout.setEnabled(false);
        comAdapter = new CommonAdapter<VFMessage>(context, R.layout.fg_history_list_item, listData) {
            @Override
            protected void convert(ViewHolder viewHolder, VFMessage msg, int position) {

                ImageView imageView = viewHolder.getView(R.id.iv_headImage);
                Glide.with(context).load("http://qlogo2.store.qq.com/qzone/6355021" + position + "/6355021" + position + "/100")
                        .transform(new GlideCircleTransform(context)).into(imageView);

                viewHolder.setText(R.id.tv_name, msg.getFrom_user().equals(Constant.Login_Name) ? msg.getTo_user() : msg.getFrom_user())
                        .setText(R.id.tv_date, Tools.transTime(msg.getTimestamp()));
                if (msg.getBodies().getType() == Body_type.txt) {
                    viewHolder.setText(R.id.tv_msg, msg.getBodies().getMsg());
                } else if (msg.getBodies().getType() == Body_type.audio) {
                    viewHolder.setText(R.id.tv_msg, "[语音]");
                } else if (msg.getBodies().getType() == Body_type.img) {
                    viewHolder.setText(R.id.tv_msg, "[图片]");
                } else if (msg.getBodies().getType() == Body_type.loc) {
                    viewHolder.setText(R.id.tv_msg, "[定位]");
                }

                TextView textView = viewHolder.getView(R.id.tv_message_num);
                if (msg.getUnreadnum() > 0) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(msg.getUnreadnum() + "");
                } else {
                    textView.setVisibility(View.GONE);
                }
            }
        };

        lv_list.setAdapter(comAdapter);
        notifyDataRef();

        lv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                VFMessage message = listData.get(position);
                BaseActivity activity = (BaseActivity) context;
                Bundle bl = new Bundle();
                bl.putSerializable("userName", Constant.Login_Name.equals(message.getFrom_user()) ? message.getTo_user() : message.getFrom_user());
                activity.openActivity(ChatActy.class, bl);
            }
        });
    }

    @Override
    public void onResult(EventBusType data) {
        if (data.getTag().equals(EBConstant.CHATMESSAGE) || data.getTag().equals(EBConstant.CONVERSATIONS)) {
            notifyDataRef();
        }
    }

    @Override
    public void reqData() {

    }

    //更新数据
    private void notifyDataRef() {
        listData.clear();
        listData.addAll(conversationDao.getAllConversation());
        comAdapter.notifyDataSetChanged();
    }


}
