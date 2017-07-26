package com.centersoft.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.centersoft.base.ChatApplication;
import com.centersoft.db.dao.ConversationDao;
import com.centersoft.db.dao.VFMessageDao;
import com.centersoft.entity.Bodies;
import com.centersoft.entity.VFMessage;
import com.centersoft.enums.Chat_type;
import com.centersoft.util.Constant;
import com.centersoft.util.MyLog;

import java.util.ArrayList;
import java.util.List;

public class DBManager {

    static private DBManager dbMgr = new DBManager();
    private DbOpenHelper dbHelper;

    private DBManager() {
        dbHelper = DbOpenHelper.getInstance(ChatApplication.getApplication());
    }

    public static synchronized DBManager getInstance() {
        if (dbMgr == null) {
            dbMgr = new DBManager();
        }
        return dbMgr;
    }


    /**
     * 保存message
     *
     * @param message
     * @return 返回这条messaged在db中的id
     */
    public synchronized Integer saveMessage(VFMessage message) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int id = -1;
        if (db.isOpen()) {

            ContentValues values = new ContentValues();

            values.put(VFMessageDao.COLUMN_NAME_GROUP_ID, message.getGroup_id());
            values.put(VFMessageDao.COLUMN_NAME_GROUP_NAME, message.getGroup_name());

            values.put(VFMessageDao.COLUMN_NAME_MESSAGE_ID, message.getMsg_id());
            values.put(VFMessageDao.COLUMN_NAME_TIMESTAMP, message.getTimestamp());

            values.put(VFMessageDao.COLUMN_NAME_FROM_USER, message.getFrom_user());
            values.put(VFMessageDao.COLUMN_NAME_TO_USER, message.getTo_user());

            values.put(VFMessageDao.COLUMN_NAME_STATUS, message.getStatus());
            values.put(VFMessageDao.COLUMN_NAME_CHAT_TYPE, message.getChat_type().toString());
            values.put(VFMessageDao.COLUMN_NAME_EXT, message.getExt());
            values.put(VFMessageDao.COLUMN_NAME_BODIES, JSON.toJSONString(message.getBodies()));

            db.insert(VFMessageDao.TABLE_NAME, null, values);

            Cursor cursor = db.rawQuery("select last_insert_rowid() from " + VFMessageDao.TABLE_NAME, null);
            if (cursor.moveToFirst()) {
                id = cursor.getInt(0);
            }
            MyLog.i("save-message", "保存成功 " + id);
            cursor.close();
        }
        return id;
    }

    /**
     * 保存 List message
     *
     * @return 返回这条messaged在db中的id
     */
    public synchronized Integer saveListMessage(List<VFMessage> messages) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int id = -1;
        if (db.isOpen()) {

            for (VFMessage message : messages) {

                ContentValues values = new ContentValues();

                values.put(VFMessageDao.COLUMN_NAME_GROUP_ID, message.getGroup_id());
                values.put(VFMessageDao.COLUMN_NAME_GROUP_NAME, message.getGroup_name());

                values.put(VFMessageDao.COLUMN_NAME_MESSAGE_ID, message.getMsg_id());
                values.put(VFMessageDao.COLUMN_NAME_TIMESTAMP, message.getTimestamp());

                values.put(VFMessageDao.COLUMN_NAME_FROM_USER, message.getFrom_user());
                values.put(VFMessageDao.COLUMN_NAME_TO_USER, message.getTo_user());

                values.put(VFMessageDao.COLUMN_NAME_STATUS, message.getStatus());
                values.put(VFMessageDao.COLUMN_NAME_CHAT_TYPE, message.getChat_type().toString());
                values.put(VFMessageDao.COLUMN_NAME_EXT, message.getExt());
                values.put(VFMessageDao.COLUMN_NAME_BODIES, JSON.toJSONString(message.getBodies()));

                db.insert(VFMessageDao.TABLE_NAME, null, values);
            }

            Cursor cursor = db.rawQuery("select last_insert_rowid() from " + VFMessageDao.TABLE_NAME, null);
            if (cursor.moveToFirst()) {
                id = cursor.getInt(0);
            }

            cursor.close();
        }
        return id;
    }


    /**
     * 更新message
     *
     * @param msgId
     * @param values
     */
    synchronized public void updateMessageWithMsgId(String msgId, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.update(VFMessageDao.TABLE_NAME, values, VFMessageDao.COLUMN_NAME_MESSAGE_ID + " = ?", new String[]{String.valueOf(msgId)});
        }
    }

    /**
     * 获取当前 对应的聊天人的 历史聊天记录
     *
     * @return
     */
    synchronized public List<VFMessage> getMessageListWithToUser(String fromUser, String toUser, int offset, int limit) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<VFMessage> msgs = new ArrayList<VFMessage>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from (select * from " + VFMessageDao.TABLE_NAME
                    + " where from_user in (?,?) and to_user in(?,?) order by id DESC limit ?,? ) order by id asc", new String[]{
                    fromUser, toUser, fromUser, toUser, offset + "", limit + ""
            });

            while (cursor.moveToNext()) {

                int id = cursor.getInt(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_ID));

                String groupid = cursor.getString(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_GROUP_ID));
                String groupname = cursor.getString(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_GROUP_NAME));

                String msg_id = cursor.getString(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_MESSAGE_ID));
                long timestamp = cursor.getLong(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_TIMESTAMP));

                String from_user = cursor.getString(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_FROM_USER));
                String to_user = cursor.getString(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_TO_USER));
                String status = cursor.getString(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_STATUS));

                Chat_type chat_type = Chat_type.valueOf(cursor.getString(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_CHAT_TYPE)));
                String ext = cursor.getString(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_EXT));
                Bodies bodies = JSON.parseObject(cursor.getString(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_BODIES)), Bodies.class);


                VFMessage msg = new VFMessage(from_user, to_user, chat_type, ext, bodies);

                msg.setId(id);
                msg.setGroup_id(groupid);
                msg.setGroup_name(groupname);
                msg.setTimestamp(timestamp);
                msg.setStatus(status);
                msg.setMsg_id(msg_id);

                msgs.add(msg);
            }
            cursor.close();
        }
        return msgs;
    }

    /**
     * 获取messges
     *
     * @return
     */
    synchronized public List<VFMessage> getAllMessagesList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<VFMessage> msgs = new ArrayList<VFMessage>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from " + VFMessageDao.TABLE_NAME + " order by id asc", null);
            while (cursor.moveToNext()) {

                int id = cursor.getInt(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_ID));

                String groupid = cursor.getString(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_GROUP_ID));
                String groupname = cursor.getString(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_GROUP_NAME));

                String msg_id = cursor.getString(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_MESSAGE_ID));
                long timestamp = cursor.getLong(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_TIMESTAMP));

                String from_user = cursor.getString(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_FROM_USER));
                String to_user = cursor.getString(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_TO_USER));
                String status = cursor.getString(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_STATUS));

                Chat_type chat_type = Chat_type.valueOf(cursor.getString(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_CHAT_TYPE)));
                String ext = cursor.getString(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_EXT));
                Bodies bodies = JSON.parseObject(cursor.getString(cursor.getColumnIndex(VFMessageDao.COLUMN_NAME_BODIES)), Bodies.class);


                VFMessage msg = new VFMessage(from_user, to_user, chat_type, ext, bodies);

                msg.setId(id);
                msg.setGroup_id(groupid);
                msg.setGroup_name(groupname);
                msg.setTimestamp(timestamp);
                msg.setStatus(status);
                msg.setMsg_id(msg_id);

                msgs.add(msg);
            }
            cursor.close();
        }
        return msgs;
    }


    /**
     * 根据msg 删除 对应的消息数据
     */
    public synchronized void deleteMessageWithMsgId(String msgId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.delete(VFMessageDao.TABLE_NAME, VFMessageDao.COLUMN_NAME_MESSAGE_ID + " = ?", new String[]{msgId});
        }
    }


    /**** ------------------------- 会话列表 start  ---------------------------------****/


    /**
     * 保存历史会话
     */
    public synchronized int saveOrUpdateConversation(VFMessage message) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int num = 0;
        if (db.isOpen()) {

            Cursor c = db.rawQuery("SELECT from_user, msg_id,unreadnum FROM " + ConversationDao.TABLE_NAME
                    + " WHERE " + ConversationDao.COLUMN_NAME_FROM_USER + " in (?,?) and " + ConversationDao.COLUMN_NAME_TO_USER
                    + " in (?,?)", new String[]{message.getFrom_user(), message.getTo_user(), message.getFrom_user(), message.getTo_user()});

            ContentValues values = new ContentValues();

            values.put(ConversationDao.COLUMN_NAME_GROUP_ID, message.getGroup_id());
            values.put(ConversationDao.COLUMN_NAME_GROUP_NAME, message.getGroup_name());

            values.put(ConversationDao.COLUMN_NAME_MESSAGE_ID, message.getMsg_id());
            values.put(ConversationDao.COLUMN_NAME_TIMESTAMP, message.getTimestamp());

            values.put(ConversationDao.COLUMN_NAME_FROM_USER, message.getFrom_user());
            values.put(ConversationDao.COLUMN_NAME_TO_USER, message.getTo_user());

            values.put(ConversationDao.COLUMN_NAME_STATUS, message.getStatus());
            values.put(ConversationDao.COLUMN_NAME_CHAT_TYPE, message.getChat_type().toString());
            values.put(ConversationDao.COLUMN_NAME_EXT, message.getExt());
            values.put(ConversationDao.COLUMN_NAME_BODIES, JSON.toJSONString(message.getBodies()));

            if (c.moveToFirst()) {

                String msg_id = c.getString(c.getColumnIndex(ConversationDao.COLUMN_NAME_MESSAGE_ID));

                if ((TextUtils.isEmpty(Constant.chatToUser) || !Constant.chatToUser.equals(message.getFrom_user()))
                        && !message.getFrom_user().equals(Constant.Login_Name)) {         //不是聊天界面的时候添加未读数
                    int unreadnum = c.getInt(c.getColumnIndex(ConversationDao.COLUMN_NAME_UNREAD_NUM));
                    num = unreadnum + 1;
                    values.put(ConversationDao.COLUMN_NAME_UNREAD_NUM, num);
                }

                db.update(ConversationDao.TABLE_NAME, values, "msg_id = ?", new String[]{msg_id});

            } else {
                values.put(ConversationDao.COLUMN_NAME_UNREAD_NUM, 1);
                db.insert(ConversationDao.TABLE_NAME, null, values);

            }

            c.close();
        }
        return num;
    }


    /**
     * 设置历史会话 未读数量为0
     */
    public synchronized void updateConversationUnReadNum(String fromUser, String toUser) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        if (db.isOpen()) {

            Cursor cursor = db.rawQuery("select msg_id from " + ConversationDao.TABLE_NAME + " where from_user in (?,?) and to_user in(?,?) order by id asc", new String[]{
                    fromUser, toUser, fromUser, toUser
            });
            if (cursor.moveToFirst()) {
                String msg_id = cursor.getString(cursor.getColumnIndex(ConversationDao.COLUMN_NAME_MESSAGE_ID));
                ContentValues values = new ContentValues();
                values.put(ConversationDao.COLUMN_NAME_UNREAD_NUM, 0);
                db.update(ConversationDao.TABLE_NAME, values, "msg_id = ?", new String[]{msg_id});
            }
            cursor.close();
        }
    }


    /**
     * 获取所有会话的未读数的总条数
     */
    public synchronized int getAllConversationUnreadNum() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int num = 0;
        if (db.isOpen()) {

            Cursor cursor = db.rawQuery("select SUM(unreadnum) as countnum from " + ConversationDao.TABLE_NAME, null);
            if (cursor.moveToFirst()) {
                num = cursor.getInt(cursor.getColumnIndex("countnum"));
            }
            cursor.close();
        }
        return num;
    }


    /**
     * 根据msg id 删除 对应的历史会话
     */
    public synchronized void deleteConversationWithMsgId(String msgId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.delete(ConversationDao.TABLE_NAME, ConversationDao.COLUMN_NAME_MESSAGE_ID + " = ?", new String[]{msgId});
        }
    }


    /**
     * 查询所有的历史会话
     */
    public synchronized List<VFMessage> getAllConversation() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<VFMessage> msgs = new ArrayList<VFMessage>();
        if (db.isOpen()) {

            Cursor cursor = db.rawQuery("select * from " + ConversationDao.TABLE_NAME + " order by " + ConversationDao.COLUMN_NAME_TIMESTAMP + " desc", null);

            while (cursor.moveToNext()) {

                int id = cursor.getInt(cursor.getColumnIndex(ConversationDao.COLUMN_NAME_ID));

                String groupid = cursor.getString(cursor.getColumnIndex(ConversationDao.COLUMN_NAME_GROUP_ID));
                String groupname = cursor.getString(cursor.getColumnIndex(ConversationDao.COLUMN_NAME_GROUP_NAME));

                String msg_id = cursor.getString(cursor.getColumnIndex(ConversationDao.COLUMN_NAME_MESSAGE_ID));
                long timestamp = cursor.getLong(cursor.getColumnIndex(ConversationDao.COLUMN_NAME_TIMESTAMP));
                int unreadnum = cursor.getInt(cursor.getColumnIndex(ConversationDao.COLUMN_NAME_UNREAD_NUM));

                String from_user = cursor.getString(cursor.getColumnIndex(ConversationDao.COLUMN_NAME_FROM_USER));
                String to_user = cursor.getString(cursor.getColumnIndex(ConversationDao.COLUMN_NAME_TO_USER));
                String status = cursor.getString(cursor.getColumnIndex(ConversationDao.COLUMN_NAME_STATUS));

                Chat_type chat_type = Chat_type.valueOf(cursor.getString(cursor.getColumnIndex(ConversationDao.COLUMN_NAME_CHAT_TYPE)));
                String ext = cursor.getString(cursor.getColumnIndex(ConversationDao.COLUMN_NAME_EXT));
                Bodies bodies = JSON.parseObject(cursor.getString(cursor.getColumnIndex(ConversationDao.COLUMN_NAME_BODIES)), Bodies.class);


                VFMessage msg = new VFMessage(from_user, to_user, chat_type, ext, bodies);

                msg.setId(id);
                msg.setGroup_id(groupid);
                msg.setGroup_name(groupname);
                msg.setTimestamp(timestamp);
                msg.setStatus(status);
                msg.setMsg_id(msg_id);
                msg.setUnreadnum(unreadnum);

                msgs.add(msg);
            }
            cursor.close();
        }
        return msgs;
    }

    /**** ------------------------- 会话列表 end  ---------------------------------****/


    synchronized public void closeDB() {
        if (dbHelper != null) {
            dbHelper.closeDB();
        }
        dbMgr = null;
    }


}
