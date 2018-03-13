package com.centersoft.entity;

import com.centersoft.enums.Chat_type;

/**
 * Created by liudong on 2017/7/6.
 * 消息 entity
 */

public class VFMessage extends BaseEnty {


    private int id;
    private String msg_id;      //消息ID
    private long timestamp;     //消息发送时间
    private String from_user;        //发送人
    private String to_user;          //要发送的人
    private Chat_type chat_type;//消息类型
    private String ext;         //扩展
    private Bodies bodies;      //内容
    private String group_id;
    private String group_name;
    private String status;      //状态
    private int unreadnum;      // 未读数

    public VFMessage(String from, String to, Chat_type chat_type, String ext, Bodies bodies) {
        this.from_user = from;
        this.to_user = to;
        this.chat_type = chat_type;
        this.ext = ext;
        this.bodies = bodies;
    }

    public VFMessage(String from, String to, Chat_type chat_type, Bodies bodies) {
        this.from_user = from;
        this.to_user = to;
        this.chat_type = chat_type;
        this.bodies = bodies;
    }

    private VFMessage() {

    }


    public String getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(String msg_id) {
        this.msg_id = msg_id;
    }

    public Chat_type getChat_type() {
        return chat_type;
    }

    public void setChat_type(Chat_type chat_type) {
        this.chat_type = chat_type;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }


    public Bodies getBodies() {
        return bodies;
    }

    public void setBodies(Bodies bodies) {
        this.bodies = bodies;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFrom_user() {
        return from_user;
    }

    public void setFrom_user(String from_user) {
        this.from_user = from_user;
    }

    public String getTo_user() {
        return to_user;
    }

    public void setTo_user(String to_user) {
        this.to_user = to_user;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getUnreadnum() {
        return unreadnum;
    }

    public void setUnreadnum(int unreadnum) {
        this.unreadnum = unreadnum;
    }
}
