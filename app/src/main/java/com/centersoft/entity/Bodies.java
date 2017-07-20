package com.centersoft.entity;

import com.centersoft.enums.Body_type;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.converter.PropertyConverter;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by liudong on 2017/7/6.
 * 消息 entity
 */

@Entity
public class Bodies extends BaseEnty{

    public static class Body_typeConverter implements PropertyConverter<Body_type, String> {
        @Override
        public Body_type convertToEntityProperty(String databaseValue) {
            return Body_type.valueOf(databaseValue);
        }

        @Override
        public String convertToDatabaseValue(Body_type entityProperty) {
            return entityProperty.name();
        }
    }


    @Convert(converter = Body_typeConverter.class, columnType = String.class)
    private Body_type type;
    private String msg;             //消息内容
    @Id
    private String id;

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

    @Generated(hash = 1465916611)
    public Bodies(Body_type type, String msg, String id, String imgUrl, String imageName) {
        this.type = type;
        this.msg = msg;
        this.id = id;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
