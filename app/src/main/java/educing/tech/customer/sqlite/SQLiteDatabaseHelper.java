package educing.tech.customer.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;

import educing.tech.customer.model.Advertisement;
import educing.tech.customer.model.ChatMessage;
import educing.tech.customer.model.User;
import educing.tech.customer.session.SessionManager;


public class SQLiteDatabaseHelper extends SQLiteOpenHelper
{

    private SessionManager sessionManager;

    Context context;

    // Database version
    private static final int DATABASE_VERSION = 1;

    // Database name
    private static final String DATABASE_NAME = "EducingTechUserDB";


    public static final String TABLE_ADVERTISEMENT = "advertisement";
    public static final String TABLE_CHAT_USERS = "chat_users";
    public static final String TABLE_CHAT_MESSAGES = "chat_messages";
    public static final String TABLE_CHAT_IMAGES = "chat_images";


    // Complain table column names
    private static final String KEY_ID = "id";
    private static final String KEY_STORE_ID = "store_id";
    private static final String KEY_CATEGORY_ID = "category_id";
    public static final String KEY_USER_ID = "user_id";
    private static final String KEY_SENDER_ID = "sender_id";
    private static final String KEY_SENDER_NAME = "sender_name";
    private static final String KEY_RECIPIENT_ID = "recipient_id";
    public static final String KEY_MESSAGE_ID = "message_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_SYNC_STATUS = "sync_status";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_STORE_NAME = "store_name";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_READ_STATUS = "read_status";
    private static final String KEY_MESSAGE_TYPE = "message_type";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_PATH = "path";
    private static final String KEY_STORE_RATING = "store_rating";


    private static final String CREATE_TABLE_CHAT_USERS = "CREATE TABLE "
            + TABLE_CHAT_USERS + "(" + KEY_USER_ID + " INTEGER PRIMARY KEY," + KEY_USER_NAME + " TEXT," + KEY_TIMESTAMP + " TEXT)";


    private static final String CREATE_TABLE_CHAT_MESSAGES = "CREATE TABLE "
            + TABLE_CHAT_MESSAGES + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_MESSAGE_ID + " TEXT," + KEY_USER_ID + " INTEGER,"
            + KEY_MESSAGE + " TEXT, " + KEY_IMAGE + " TEXT, " + KEY_TIMESTAMP + " TEXT," + KEY_READ_STATUS + " INTEGER DEFAULT 0," + KEY_SYNC_STATUS + " INTEGER DEFAULT 0,"
            + KEY_MESSAGE_TYPE + " INTEGER," + " FOREIGN KEY (" + KEY_USER_ID + ") REFERENCES " + TABLE_CHAT_USERS + "(" + KEY_USER_ID + ") ON DELETE CASCADE, UNIQUE "
            + "(" + KEY_MESSAGE_ID + "))";


    private static final String CREATE_TABLE_CHAT_IMAGES = "CREATE TABLE "
            + TABLE_CHAT_IMAGES + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_MESSAGE_ID + " TEXT," + KEY_PATH + " TEXT,"
            + KEY_SYNC_STATUS + " INTEGER DEFAULT 0, FOREIGN KEY (" + KEY_MESSAGE_ID + ") REFERENCES " + TABLE_CHAT_MESSAGES + "(" + KEY_MESSAGE_ID + ") ON DELETE CASCADE)";


    private static final String CREATE_TABLE_ADVERTISEMENT = "CREATE TABLE "
            + TABLE_ADVERTISEMENT + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_STORE_ID + " INTEGER," + KEY_STORE_NAME + " TEXT," + KEY_STORE_RATING + " REAL,"
            + KEY_CATEGORY_ID + " INTEGER," + KEY_MESSAGE + " TEXT," + KEY_IMAGE + " TEXT," + KEY_TIMESTAMP + " TEXT," + KEY_READ_STATUS + " INTEGER DEFAULT 0)";


    public SQLiteDatabaseHelper(Context context)
    {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

        sessionManager = new SessionManager(context);
    }


    @Override
    public void onCreate(SQLiteDatabase database)
    {

        database.execSQL(CREATE_TABLE_ADVERTISEMENT);
        database.execSQL(CREATE_TABLE_CHAT_USERS);
        database.execSQL(CREATE_TABLE_CHAT_MESSAGES);
        database.execSQL(CREATE_TABLE_CHAT_IMAGES);
    }


    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old, int current_version)
    {

        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ADVERTISEMENT);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_USERS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_MESSAGES);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_IMAGES);

        // create new tables
        onCreate(database);
    }


    public boolean insertChatUser(ChatMessage message)
    {

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_USER_ID, message.getUserId());
        values.put(KEY_USER_NAME, message.getUserName());
        values.put(KEY_TIMESTAMP, message.getTimestamp());

        // Inserting Row
        boolean createSuccessful = database.insert(TABLE_CHAT_USERS, null, values) > 0;

        Log.v("createSuccessful", "" + createSuccessful);

        // Closing database connection
        database.close();

        return createSuccessful;
    }


    public boolean insertChatMessage(ChatMessage message, int sync_status)
    {

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_MESSAGE_ID, message.getMessageId());
        values.put(KEY_USER_ID, message.getUserId());
        values.put(KEY_MESSAGE, message.getMessage());
        values.put(KEY_IMAGE, message.getImage());
        values.put(KEY_TIMESTAMP, message.getTimestamp());
        values.put(KEY_READ_STATUS, message.getReadStatus());
        values.put(KEY_MESSAGE_TYPE, message.getMessageType());
        values.put(KEY_SYNC_STATUS, sync_status);

        // Inserting Row
        boolean createSuccessful = database.insert(TABLE_CHAT_MESSAGES, null, values) > 0;

        Log.v("createSuccessful", "" + createSuccessful);

        // Closing database connection
        database.close();

        return createSuccessful;
    }


    public boolean insertAdvertisement(Advertisement advertisement)
    {

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_STORE_ID, advertisement.getStoreId());
        values.put(KEY_STORE_NAME, advertisement.getStoreName());
        values.put(KEY_STORE_RATING, advertisement.getRating());
        values.put(KEY_CATEGORY_ID, advertisement.getCategoryId());
        values.put(KEY_MESSAGE, advertisement.getMessage());
        values.put(KEY_IMAGE, advertisement.getFileName());
        values.put(KEY_TIMESTAMP, advertisement.getTimestamp());

        // Inserting Row
        boolean createSuccessful = database.insert(TABLE_ADVERTISEMENT, null, values) > 0;

        // Closing database connection
        database.close();

        return createSuccessful;
    }


    public boolean insertChatImages(String message_id, String path)
    {

        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_MESSAGE_ID, message_id);
        values.put(KEY_PATH, path);

        // Inserting Row
        boolean createSuccessful = database.insert(TABLE_CHAT_IMAGES, null, values) > 0;

        // Closing database connection
        database.close();

        return createSuccessful;
    }


    public ArrayList<ChatMessage> getAllChatMessage(String user_id, int x, int y)
    {

        ArrayList<ChatMessage> messagesList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_CHAT_MESSAGES + " WHERE " + KEY_USER_ID + "='" + user_id
                + "' ORDER BY " + KEY_ID + " DESC LIMIT " + x + "," + y;

        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery(selectQuery, null);


        if (cursor.moveToFirst())
        {

            do
            {

                ChatMessage message = new ChatMessage();

                message.setMessageId(cursor.getString(1));
                message.setUserId(cursor.getString(2));
                message.setMessage(cursor.getString(3));
                message.setImage(cursor.getString(4));
                message.setTimestamp(cursor.getString(5));
                message.setReadStatus(cursor.getInt(6));
                message.setSyncStatus(cursor.getInt(7));
                message.setMessageType(cursor.getInt(8));

                messagesList.add(message);
            }

            while (cursor.moveToNext());
        }

        database.close();
        cursor.close();

        return messagesList;
    }



    public ChatMessage getLastMessage(String user_id)
    {

        ChatMessage message = new ChatMessage();

        String query = "SELECT " + KEY_MESSAGE + "," + KEY_TIMESTAMP + ", "
                + "(SELECT COUNT("+ KEY_READ_STATUS + ") FROM " + TABLE_CHAT_MESSAGES + " WHERE " + KEY_USER_ID + "='" + user_id + "' AND " + KEY_READ_STATUS + "='0')"
                + " FROM " + TABLE_CHAT_MESSAGES + " WHERE " + KEY_ID + "=(" + "SELECT MAX(" + KEY_ID + ") FROM " + TABLE_CHAT_MESSAGES + " WHERE " + KEY_USER_ID + "='" + user_id + "')";

        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery(query, null);

        if (cursor.moveToFirst())
        {
            message.setMessage(cursor.getString(0));
            message.setTimestamp(cursor.getString(1));
            message.setUnreadMessageCount(cursor.getInt(2));
        }

        database.close();
        cursor.close();

        return message;
    }


    public ArrayList<ChatMessage> getAllChatUsers()
    {

        ArrayList<ChatMessage> messagesList = new ArrayList<>();

        String selectQuery = "SELECT DISTINCT " + KEY_USER_ID + ", " + KEY_USER_NAME + ", " + KEY_TIMESTAMP
                + " FROM " + TABLE_CHAT_USERS + " WHERE " + KEY_USER_ID + " != 0 ORDER BY " + KEY_TIMESTAMP + " DESC";

        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery(selectQuery, null);


        if (cursor.moveToFirst())
        {

            do
            {

                ChatMessage message = new ChatMessage();

                message.setUserId(cursor.getString(0));
                message.setUserName(cursor.getString(1));
                message.setTimestamp(cursor.getString(2));

                ChatMessage temp_msg = getLastMessage(message.user_id);

                message.setMessage(temp_msg.message);
                message.setUnreadMessageCount(temp_msg.unread_message);

                messagesList.add(message);
            }

            while (cursor.moveToNext());
        }

        database.close();
        cursor.close();

        return messagesList;
    }


    public ArrayList<Advertisement> getAllAdvertisement(int x, int y)
    {

        ArrayList<Advertisement> advertisementList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_ADVERTISEMENT + " ORDER BY " + KEY_ID + " DESC LIMIT "  + x + "," + y;


        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery(selectQuery, null);


        if (cursor.moveToFirst())
        {

            do
            {

                Advertisement advertisementObj = new Advertisement();

                advertisementObj.setAdvertisementId(cursor.getInt(0));
                advertisementObj.setStoreId(cursor.getInt(1));
                advertisementObj.setStoreName(cursor.getString(2));
                advertisementObj.setRating(cursor.getDouble(3));
                advertisementObj.setCategoryId(cursor.getInt(4));
                advertisementObj.setMessage(cursor.getString(5));
                advertisementObj.setFileName(cursor.getString(6));
                advertisementObj.setTimestamp(cursor.getString(7));

                advertisementList.add(advertisementObj);
            }

            while (cursor.moveToNext());
        }

        database.close();
        cursor.close();

        return advertisementList;
    }


    public String chatMessageJSONData()
    {

        ArrayList<HashMap<String, String>> wordList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_CHAT_MESSAGES + " WHERE " + KEY_SYNC_STATUS + " = '0'";

        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery(selectQuery, null);


        if (cursor.moveToFirst())
        {

            do
            {

                HashMap<String, String> map = new HashMap<>();

                map.put(KEY_ID, cursor.getString(0));
                map.put(KEY_MESSAGE_ID, cursor.getString(1));
                map.put(KEY_RECIPIENT_ID, cursor.getString(2));
                map.put(KEY_MESSAGE, cursor.getString(3));
                map.put(KEY_IMAGE, cursor.getString(4));
                map.put(KEY_TIMESTAMP, cursor.getString(5));
                map.put(KEY_SENDER_NAME, getUserDetails().getUserName());
                map.put(KEY_SENDER_ID, String.valueOf(getUserDetails().getUserID()));

                wordList.add(map);
            }

            while (cursor.moveToNext());
        }

        database.close();
        cursor.close();

        Gson gson = new GsonBuilder().create();

        // Use GSON to serialize Array List to JSON
        return gson.toJson(wordList);
    }


    public ArrayList<ChatMessage> getAllChatImages()
    {

        ArrayList<ChatMessage> numberList = new ArrayList<>();

        String selectQuery = "SELECT " + TABLE_CHAT_IMAGES + "." + KEY_MESSAGE_ID + ", " + KEY_PATH + "," + KEY_IMAGE
                + " FROM " + TABLE_CHAT_MESSAGES + " LEFT JOIN " + TABLE_CHAT_IMAGES + " ON " + TABLE_CHAT_MESSAGES + "." + KEY_MESSAGE_ID
                + "=" + TABLE_CHAT_IMAGES + "." + KEY_MESSAGE_ID + " WHERE " + TABLE_CHAT_IMAGES + "." + KEY_SYNC_STATUS + "='0'";


        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery(selectQuery, null);


        if (cursor.moveToFirst())
        {

            do
            {

                ChatMessage numberObj = new ChatMessage();

                numberObj.setMessageId(cursor.getString(0));
                numberObj.setFilePath(cursor.getString(1));
                numberObj.setImage(cursor.getString(2));

                numberList.add(numberObj);
            }

            while (cursor.moveToNext());
        }

        database.close();
        cursor.close();

        return numberList;
    }


    public int dbSyncCount(String TABLE_NAME)
    {

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_SYNC_STATUS + " = '0'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        int count = cursor.getCount();
        database.close();
        cursor.close();

        return count;
    }


    public int dbRowCount(String TABLE_NAME)
    {

        String selectQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        int count = cursor.getCount();
        database.close();
        cursor.close();

        return count;
    }


    public int dbRowCount(String TABLE_NAME, String COLUMN_NAME, String value)
    {

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME + "='" + value + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        int count = cursor.getCount();
        database.close();
        cursor.close();

        return count;
    }


    public int unreadMessageCount(String TABLE_NAME)
    {

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_READ_STATUS + "='0'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        int count = cursor.getCount();
        database.close();
        cursor.close();

        return count;
    }


    public void updateSyncStatus(String TABLE_NAME, int id, int status)
    {

        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "UPDATE " + TABLE_NAME + " SET " + KEY_SYNC_STATUS + " = '" + status + "' WHERE " + KEY_ID + " = '" + id + "'";
        Log.d("query",updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }


    public void updateSyncStatus(String TABLE_NAME, String COLUMN_NAME, String id, int status)
    {

        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "UPDATE " + TABLE_NAME + " SET " + KEY_SYNC_STATUS + " = '" + status + "' WHERE " + COLUMN_NAME + " = '" + id + "'";
        Log.d("query",updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }


    public void updateChatUser(ChatMessage user)
    {

        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "UPDATE " + TABLE_CHAT_USERS + " SET " + KEY_USER_NAME + " = '" + user.user_name + "'," + KEY_TIMESTAMP + " = '" + user.timestamp + "' WHERE " + KEY_USER_ID + " = '" + user.user_id + "'";
        Log.d("query",updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }


    public void clearChatMessages(String user_id)
    {

        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "DELETE FROM " + TABLE_CHAT_MESSAGES + " WHERE " + KEY_USER_ID + "='" + user_id + "'";
        Log.d("query",updateQuery);
        database.execSQL("PRAGMA foreign_keys=ON");
        database.execSQL(updateQuery);
        database.close();
    }


    public void clearChatUsers()
    {

        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "DELETE FROM " + TABLE_CHAT_USERS;
        Log.d("query",updateQuery);
        database.execSQL("PRAGMA foreign_keys=ON");
        database.execSQL(updateQuery);
        database.close();
    }


    public void clearChatUsers(String user_id)
    {

        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "DELETE FROM " + TABLE_CHAT_USERS + " WHERE " + KEY_USER_ID + "='" + user_id + "'";
        Log.d("query",updateQuery);
        database.execSQL("PRAGMA foreign_keys=ON");
        database.execSQL(updateQuery);
        database.close();
    }


    public void clearAllAdvertisement()
    {

        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "DELETE FROM " + TABLE_ADVERTISEMENT;
        Log.d("query",updateQuery);
        database.execSQL("PRAGMA foreign_keys=ON");
        database.execSQL(updateQuery);
        database.close();
    }


    public void clearAdvertisement(int advertisement_id)
    {

        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "DELETE FROM " + TABLE_ADVERTISEMENT + " WHERE " + KEY_ID + "='" + advertisement_id + "'";
        Log.d("query",updateQuery);
        database.execSQL("PRAGMA foreign_keys=ON");
        database.execSQL(updateQuery);
        database.close();
    }


    public int setAsRead(String user_id)
    {

        String selectQuery = "UPDATE " + TABLE_CHAT_MESSAGES + " SET " + KEY_READ_STATUS + "='1' WHERE " + KEY_USER_ID + "='" + user_id + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        int count = cursor.getCount();
        database.close();
        cursor.close();

        return count;
    }


    public int setAsRead()
    {

        String selectQuery = "UPDATE " + TABLE_ADVERTISEMENT + " SET " + KEY_READ_STATUS + "='1'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        int count = cursor.getCount();
        database.close();
        cursor.close();

        return count;
    }


    private User getUserDetails()
    {

        User userObj = new User();

        if (sessionManager.checkLogin())
        {
            HashMap<String, String> user = sessionManager.getUserDetails();

            userObj.setUserID(Integer.valueOf(user.get(SessionManager.KEY_USER_ID)));
            userObj.setPhoneNo(user.get(SessionManager.KEY_PHONE));
            userObj.setUserName(user.get(SessionManager.KEY_USER_NAME));
        }

        return userObj;
    }
}