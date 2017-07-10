package com.centersoft.entity;

import java.io.Serializable;

/**
 * Created by liudong on 2017/7/6.
 * 消息 entity
 */

public class VFMessage implements Serializable {


    public static enum Chat_type {   //聊天的类型
        chat,           //单聊
        groupChat,      //群聊
    }

    public static enum Body_type {   //消息体 类型
        txt,
        img,
    }


    public static class Bodies {        //消息体

        Body_type type;
        String msg;             //消息内容

        String imgUrl;             //imageUrl
        String imageName;       //imageName

        public Bodies(Body_type type, String msg) {
            this.type = type;
            this.msg = msg;
        }

        public Bodies(Body_type type, String imgUrl, String imageName) {
            this.type = type;
            this.imgUrl = imgUrl;
            this.imageName = imageName;
        }


        public Body_type getType() {
            return type;
        }

        public void setType(Body_type type) {
            this.type = type;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getImgUrl() {
            return imgUrl;
        }

        public void setImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
        }

        public String getImageName() {
            return imageName;
        }

        public void setImageName(String imageName) {
            this.imageName = imageName;
        }
    }


    String msg_id;      //消息ID
    long timestamp;     //消息发送时间
    String from;        //发送人
    String to;          //要发送的人
    Chat_type chat_type;//消息类型
    String ext;         //扩展
    Bodies bodies;      //内容

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
