package com.centersoft.db.dao;

import com.centersoft.db.DBManager;
import com.centersoft.entity.VFMessage;

import java.util.List;

/**
 * Description    服务器配置DAO层
 * FileName       VFMessageDao.java
 * CopyRight      CenterSoft
 * Author         LH
 * Createdate     2015年6月19日 上午10:11:21
 * ------------------------------
 * updateAuthor   <修改人员>
 * updateDate     <修改日期>
 * updateNeedNum  <需求单号>
 * updateContent  <修改内容>
 * ------------------------------
 */
public class VFMessageDao {


    // message 数据库相关
    public static final String TABLE_NAME = "vifu_message";

    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_GROUP_ID = "group_id";
    public static final String COLUMN_NAME_GROUP_NAME = "group_name";
    public static final String COLUMN_NAME_MESSAGE_ID = "msg_id";
    public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    public static final String COLUMN_NAME_FROM_USER = "from_user";
    public static final String COLUMN_NAME_TO_USER = "to_user";
    public static final String COLUMN_NAME_STATUS = "status";   //状态

    public static final String COLUMN_NAME_CHAT_TYPE = "chat_type";
    public static final String COLUMN_NAME_EXT = "ext";
    public static final String COLUMN_NAME_BODIES = "bodies";


    public VFMessageDao() {

    }

    /**
     * 保存message
     */
    public Integer saveMessage(VFMessage message){
        return DBManager.getInstance().saveMessage(message);
    }

    /**
     * 保存message
     */
    public Integer saveListMessage(List<VFMessage> message){
        return DBManager.getInstance().saveListMessage(message);
    }

    /**
     * 获取所有消息
     */
    public List<VFMessage> getAllMessageList(){
      return DBManager.getInstance().getAllMessagesList();
    }


}
