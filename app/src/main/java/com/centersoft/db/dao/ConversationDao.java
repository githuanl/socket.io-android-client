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
public class ConversationDao {


    // message 数据库相关
    public static final String TABLE_NAME = "vifu_conversation";

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
    public static final String COLUMN_NAME_UNREAD_NUM = "unreadnum"; //未读数量

    public ConversationDao() {

    }

    /**
     * 添加历史会话
     */
    public int saveOrUpdateConversation(VFMessage message) {
        return DBManager.getInstance().saveOrUpdateConversation(message);
    }


    /**
     * 更新 未读的数量
     */
    public void updateConversationUnReadNum(String fromUser, String toUser) {
        DBManager.getInstance().updateConversationUnReadNum(fromUser, toUser);
    }


    /**
     * 获取所有会话的未读数的总条数
     */
    public int getAllConversationUnreadNum() {
        return DBManager.getInstance().getAllConversationUnreadNum();
    }


    /**
     * 删除历史会话
     */
    public void deleteConversationWithMsgId(String msg_id) {
        DBManager.getInstance().deleteConversationWithMsgId(msg_id);
    }

    /**
     * 获取所有历史会话
     */
    public List<VFMessage> getAllConversation() {
        return DBManager.getInstance().getAllConversation();
    }

}
