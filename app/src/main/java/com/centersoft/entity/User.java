package com.centersoft.entity;

import com.alibaba.fastjson.annotation.JSONField;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by liudong on 2017/7/18.
 */

@Entity
public class User extends BaseEnty {

    private String id;
    private String userId;
    private String name;
    private String headImageUrl;
    private String nickname;
    private String auth_token;
    private long auth_date;
    private boolean isOnline = false;   // 是否在线

    @Generated(hash = 810388287)
    public User(String id, String userId, String name, String headImageUrl,
            String nickname, String auth_token, long auth_date, boolean isOnline) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.headImageUrl = headImageUrl;
        this.nickname = nickname;
        this.auth_token = auth_token;
        this.auth_date = auth_date;
        this.isOnline = isOnline;
    }

    @Generated(hash = 586692638)
    public User() {
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getId() {
        return id;
    }

    @JSONField(name = "_id")
    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeadImageUrl() {
        return headImageUrl;
    }

    public void setHeadImageUrl(String headImageUrl) {
        this.headImageUrl = headImageUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAuth_token() {
        return auth_token;
    }

    public void setAuth_token(String auth_token) {
        this.auth_token = auth_token;
    }

    public long getAuth_date() {
        return auth_date;
    }

    public void setAuth_date(long auth_date) {
        this.auth_date = auth_date;
    }

    public boolean getIsOnline() {
        return this.isOnline;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }


}
