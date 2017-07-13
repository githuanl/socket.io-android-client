package com.centersoft.entity;

import com.centersoft.enums.Chat_type;

import java.io.Serializable;

/**
 * Created by liudong on 2017/7/6.
 * 消息 entity
 */

public class VFMessage implements Serializable {



    private String msg_id;      //消息ID
    private  long timestamp;     //消息发送时间
    private String from;        //发送人
    private  String to;          //要发送的人
    private Chat_type chat_type;//消息类型
    private String ext;         //扩展
    private Bodies bodies;      //内容

    public VFMessage(String from, String to, Chat_type chat_type, String ext, Bodies bodies) {
        this.from = from;
        this.to = to;
        this.chat_type = chat_type;
        this.ext = ext;
        this.bodies = bodies;
    }

    public VFMessage(String from, String to, Chat_type chat_type, Bodies bodies) {
        this.from = from;
        this.to = to;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
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
}
