package com.centersoft.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.centersoft.db.dao.VFMessageDao;
import com.centersoft.util.Constant;

/**
 * Created by liudong on 2017/7/21.
 */

public class DbOpenHelper extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 1;
    private static DbOpenHelper instance;

    public static DbOpenHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DbOpenHelper(context.getApplicationContext());
        }
        return instance;
    }

    private static final String INIVTE_MESSAGE_TABLE_CREATE = "CREATE TABLE "
            + VFMessageDao.TABLE_NAME + " ("
            + VFMessageDao.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + VFMessageDao.COLUMN_NAME_GROUP_ID + " TEXT, "
            + VFMessageDao.COLUMN_NAME_GROUP_NAME + " TEXT, "
            + VFMessageDao.COLUMN_NAME_MESSAGE_ID + " TEXT, "
            + VFMessageDao.COLUMN_NAME_TIMESTAMP + " INTEGER, "
            + VFMessageDao.COLUMN_NAME_FROM_USER + " TEXT, "
            + VFMessageDao.COLUMN_NAME_TO_USER + " TEXT, "
            + VFMessageDao.COLUMN_NAME_STATUS + " INTEGER, "    //状态
            + VFMessageDao.COLUMN_NAME_CHAT_TYPE + " TEXT, "
            + VFMessageDao.COLUMN_NAME_EXT + " TEXT, "
            + VFMessageDao.COLUMN_NAME_BODIES + " TEXT); ";

    /**
     * 数据库创建的构造方法
     * 数据库的名称和版本
     *
     * @param context
     */
    public DbOpenHelper(Context context) {
        super(context, getUserDatabaseName(), null, DATABASE_VERSION);
    }


    private static String getUserDatabaseName() {
        return Constant.getLoginName() + "_vifu_chat.db";
    }

    /*
     * 初始化数据库的表结构
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(INIVTE_MESSAGE_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        if(oldVersion < 2){
//            db.execSQL("ALTER TABLE "+ UserDao.TABLE_NAME +" ADD COLUMN "+
//                    UserDao.COLUMN_NAME_AVATAR + " TEXT ;");
//        }
//
//        if(oldVersion < 3){
//            db.execSQL(CREATE_PREF_TABLE);
//        }
//        if(oldVersion < 4){
//            db.execSQL(ROBOT_TABLE_CREATE);
//        }
//        if(oldVersion < 5){
//            db.execSQL("ALTER TABLE " + InviteMessgeDao.TABLE_NAME + " ADD COLUMN " +
//                    InviteMessgeDao.COLUMN_NAME_UNREAD_MSG_COUNT + " INTEGER ;");
//        }
//        if (oldVersion < 6) {
//            db.execSQL("ALTER TABLE " + InviteMessgeDao.TABLE_NAME + " ADD COLUMN " +
//                    InviteMessgeDao.COLUMN_NAME_GROUPINVITER + " TEXT;");
//        }
    }


    public void closeDB() {
        if (instance != null) {
            try {
                SQLiteDatabase db = instance.getWritableDatabase();
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            instance = null;
        }
    }
}
