package com.centersoft.entity;

import com.centersoft.dao.BodiesDao;
import com.centersoft.dao.DaoSession;
import com.centersoft.dao.VFMessageDao;
import com.centersoft.enums.Chat_type;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.converter.PropertyConverter;

/**
 * Created by liudong on 2017/7/6.
 * 消息 entity
 */

@Entity
public class VFMessage extends BaseEnty {


    public static class Chat_typeConverter implements PropertyConverter<Chat_type, String> {
        @Override
        public Chat_type convertToEntityProperty(String databaseValue) {
            return Chat_type.valueOf(databaseValue);
        }

        @Override
        public String convertToDatabaseValue(Chat_type entityProperty) {
            return entityProperty.name();
        }
    }

    private String msg_id;      //消息ID
    private Long timestamp;     //消息发送时间
    private String from;        //发送人
    private String to;          //要发送的人

    @Convert(converter = Chat_typeConverter.class, columnType = String.class)
    private Chat_type chat_type;//消息类型
    private String ext;         //扩展

    @ToOne(joinProperty = "msg_id")
    private Bodies bodies;      //内容
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 2036564537)
    private transient VFMessageDao myDao;
    @Generated(hash = 1003603163)
    private transient String bodies__resolvedKey;

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

    @Generated(hash = 1369984987)
    public VFMessage(String msg_id, Long timestamp, String from, String to,
            Chat_type chat_type, String ext) {
        this.msg_id = msg_id;
        this.timestamp = timestamp;
        this.from = from;
        this.to = to;
        this.chat_type = chat_type;
        this.ext = ext;
    }

    public String getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(String msg_id) {
        this.msg_id = msg_id;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
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


    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1420543706)
    public Bodies getBodies() {
        String __key = this.msg_id;
        if (bodies__resolvedKey == null || bodies__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            BodiesDao targetDao = daoSession.getBodiesDao();
            Bodies bodiesNew = targetDao.load(__key);
            synchronized (this) {
                bodies = bodiesNew;
                bodies__resolvedKey = __key;
            }
        }
        return bodies;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2141575900)
    public void setBodies(Bodies bodies) {
        synchronized (this) {
            this.bodies = bodies;
            msg_id = bodies == null ? null : bodies.getId();
            bodies__resolvedKey = msg_id;
        }
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 991492378)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getVFMessageDao() : null;
    }

}
