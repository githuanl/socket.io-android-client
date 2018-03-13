package com.centersoft.entity;

/**
 * Created by liudong on 2017/7/18.
 */

public class User extends BaseEnty {

    private int id;
    private String userId;
    private String name;
    private String headImageUrl;
    private String nickname;
    private String auth_token;
    private long auth_date;             //token 过期日期
    private boolean isOnline = false;   // 是否在线

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
