package com.centersoft.entity;

import com.centersoft.enums.Body_type;

import java.io.Serializable;

/**
 * Created by liudong on 2017/7/6.
 * 消息 entity
 */

public class Bodies implements Serializable {


    private Body_type type;
    private String msg;             //消息内容

    private String imgUrl;             //imageUrl
    private String imageName;       //imageName

    public Bodies(Body_type type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public Bodies(Body_type type, String imgUrl, String imageName) {
        this.type = type;
        this.imgUrl = imgUrl;
        this.imageName = imageName;
    }

    public Bodies(){

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
