package com.centersoft.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.SPUtils;
import com.bumptech.glide.Glide;
import com.centersoft.ICallBack.IDataCallBack;
import com.centersoft.base.BaseActivity;
import com.centersoft.base.ChatApplication;
import com.centersoft.entity.EventBusType;
import com.centersoft.entity.User;
import com.centersoft.entity.VFMessage;
import com.centersoft.enums.Body_type;
import com.centersoft.enums.VFCode;
import com.centersoft.util.Constant;
import com.centersoft.util.EBConstant;
import com.centersoft.util.GlideImageLoader;
import com.centersoft.util.MyLog;
import com.centersoft.util.NetTool;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;
import com.zhy.adapter.abslistview.MultiItemTypeAdapter;
import com.zhy.adapter.abslistview.ViewHolder;
import com.zhy.adapter.abslistview.base.ItemViewDelegate;

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

//聊天界面
public class ChatActy extends BaseActivity {

    private static final int IMAGE_PICKER = 2;

    @BindView(R.id.lv_list)
    ListView lv_list;

    List<VFMessage> listData;
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

    String toUser = "";
    User user;


    @Override
    protected void initData() {

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            user = (User) bundle.getSerializable("user");
            toobarTitle.setText(user.getName());
            toUser = user.getName();
        }

        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
        imagePicker.setShowCamera(true);  //显示拍照按钮
        imagePicker.setCrop(true);        //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true); //是否按矩形区域保存
        imagePicker.setSelectLimit(1);    //选中数量限制
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
        adapter.notifyDataSetChanged();


        socket = ChatApplication.getSocket();
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
            return !item.getFrom_user().equals(Constant.getLoginName());
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
            return item.getFrom_user().equals(Constant.getLoginName());
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

            obj.put("from_user", Constant.getLoginName());
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

                        MyLog.i(Tag + "====>", args[0].toString());
                        if (args.length > 1) {
                            Ack ack = (Ack) args[args.length - 1];
                            ack.call();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

            VFMessage message = JSON.parseObject(obj.toString(), VFMessage.class);


            messageInput.setText("");

            listData.add(message);
            adapter.notifyDataSetChanged();

        } catch (JSONException e) {

        }
    }

    @Override
    public void onResult(EventBusType data) {
        if (data.getTag().equals(EBConstant.CHATMESSAGE)) {
            listData.add((VFMessage) data.getE());
            adapter.notifyDataSetChanged();
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == IMAGE_PICKER) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                final ImageItem im = images.get(0);

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


            } else {
                Toast.makeText(this, "没有数据", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!socket.connected()) { //示连接则重新连接
            socket.open();
        }
    }

}
