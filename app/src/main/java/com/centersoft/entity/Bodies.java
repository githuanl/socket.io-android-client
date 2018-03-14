package com.centersoft.entity;

import com.centersoft.enums.Body_type;

/**
 * Created by liudong on 2017/7/6.
 * 消息 entity
 */

public class Bodies extends BaseEnty {

    private Body_type type;
    private String msg;             //消息内容

    private String imgUrl;          //imageUrl
    private String imageName;       //imageName

    /*图片*/
    private String thumbnailRemotePath; //缩略图
    private String originImagePath; //原始路径

    /*位置*/
    private double latitude;
    private double longitude;
    private String locationName;
    private String detailLocationName;


    private Byte[] fileData;
    private String fileName;
    private String fileRemotePath;

    /*语音*/
    private int duration;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileRemotePath() {
        return fileRemotePath;
    }

    public void setFileRemotePath(String fileRemotePath) {
        this.fileRemotePath = fileRemotePath;
    }

    public Byte[] getFileData() {
        return fileData;
    }

    public void setFileData(Byte[] fileData) {
        this.fileData = fileData;
    }

    public Bodies(Body_type type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public Bodies(Body_type type, String imgUrl, String imageName) {
        this.type = type;
        this.imgUrl = imgUrl;
        this.imageName = imageName;
    }

    public Bodies() {

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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


    public String getThumbnailRemotePath() {
        return thumbnailRemotePath;
    }

    public void setThumbnailRemotePath(String thumbnailRemotePath) {
        this.thumbnailRemotePath = thumbnailRemotePath;
    }

    public String getOriginImagePath() {
        return originImagePath;
    }

    public void setOriginImagePath(String originImagePath) {
        this.originImagePath = originImagePath;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getDetailLocationName() {
        return detailLocationName;
    }

    public void setDetailLocationName(String detailLocationName) {
        this.detailLocationName = detailLocationName;
    }
}
