package cz.org.shoosh.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cz.org.shoosh.models.ContactModel;
import cz.org.shoosh.models.FeedModel;
import cz.org.shoosh.models.ThreadModel;

public class MySQLiteHelper extends SQLiteOpenHelper {
    private static final String TAG = "MySQLiteHelper";
    // Database Info
    private static final String DATABASE_NAME = "ShooshDB";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_FEEDS = "feeds";
    private static final String TABLE_THREADS = "threads";

    // Feeds Table Columns
    private static final String KEY_FEED_NAME = "name";
    private static final String KEY_FEED_PHONE = "phone";
    private static final String KEY_FEED_STIME = "sendtime";
    private static final String KEY_FEED_UID = "uid";

    // Threads Table Columns
    private static final String KEY_THREAD_NAME = "name";
    private static final String KEY_THREAD_PHONE = "phone";
    private static final String KEY_THREAD_STIME = "sendtime";
    private static final String KEY_THREAD_UID = "uid";
    private static final String KEY_THREAD_MSG = "msg";

    private static MySQLiteHelper sInstance;

    public static MySQLiteHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MySQLiteHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        //db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FEEDS_TABLE = "CREATE TABLE " + TABLE_FEEDS + " (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT, " + // Define a primary key
                KEY_FEED_NAME + " TEXT," +
                KEY_FEED_PHONE + " TEXT," +
                KEY_FEED_STIME + " TEXT," +
                KEY_FEED_UID + " TEXT" +
                ");";

        String CREATE_THREADS_TABLE = "CREATE TABLE " + TABLE_THREADS + " (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT, " + // Define a primary key
                KEY_THREAD_NAME + " TEXT," +
                KEY_THREAD_PHONE + " TEXT," +
                KEY_THREAD_STIME + " TEXT," +
                KEY_THREAD_UID + " TEXT," +
                KEY_THREAD_MSG + " TEXT" +
                ");";

        try{
            db.execSQL(CREATE_FEEDS_TABLE);
            db.execSQL(CREATE_THREADS_TABLE);
        }catch (Exception e){
            Log.d(TAG,e.getMessage());
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEEDS + ";");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_THREADS + ";");
            onCreate(db);
        }
    }

    public void addThread(ThreadModel thread){

        List<ThreadModel> allthreads = getAllThreads();
        for(ThreadModel one : allthreads){
            if(one.sid.equals(thread.sid)){
                SQLiteDatabase db1 = this.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(KEY_THREAD_NAME, thread.sname);
                values.put(KEY_THREAD_PHONE, thread.sphone);
                values.put(KEY_THREAD_STIME, thread.stime);
                values.put(KEY_THREAD_UID, thread.sid);
                values.put(KEY_THREAD_MSG, thread.msg);

                // Updating profile picture url for user with that userName
                db1.update(TABLE_THREADS, values, "id = ?", new String[] { String.valueOf(one.key)});
                return;
            }
        }
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            // The user might already exist in the database (i.e. the same user created multiple posts).

            ContentValues values = new ContentValues();
            values.put(KEY_THREAD_NAME, thread.sname);
            values.put(KEY_THREAD_PHONE, thread.sphone);
            values.put(KEY_THREAD_STIME, thread.stime);
            values.put(KEY_THREAD_UID, thread.sid);
            values.put(KEY_THREAD_MSG, thread.msg);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_THREADS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add quote to database");
        } finally {
            db.endTransaction();
        }
    }

    // Get all posts in the database
    public List<ThreadModel> getAllThreads() {
        List<ThreadModel> threadlist = new ArrayList<>();

        String QUOTES_SELECT_QUERY = String.format("SELECT * FROM %s;", TABLE_THREADS);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(QUOTES_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    ThreadModel newThread = new ThreadModel();
                    newThread.key = cursor.getInt(cursor.getColumnIndex("id"));
                    newThread.sname = cursor.getString(cursor.getColumnIndex(KEY_THREAD_NAME));
                    newThread.sphone = cursor.getString(cursor.getColumnIndex(KEY_THREAD_PHONE));
                    newThread.stime = cursor.getString(cursor.getColumnIndex(KEY_THREAD_STIME));
                    newThread.sid = cursor.getString(cursor.getColumnIndex(KEY_THREAD_UID));
                    newThread.msg = cursor.getString(cursor.getColumnIndex(KEY_THREAD_MSG));

                    threadlist.add(newThread);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Quotes from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return threadlist;
    }

    public void deleteThread(int key){
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_THREADS, "id='" + String.valueOf(key) + "'", null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete all posts and users");
        } finally {
            db.endTransaction();
        }

    }

    public void addFeed(FeedModel feed){
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            // The user might already exist in the database (i.e. the same user created multiple posts).

            ContentValues values = new ContentValues();
            values.put(KEY_FEED_NAME, feed.uname);
            values.put(KEY_FEED_PHONE, feed.phoneno);
            values.put(KEY_FEED_STIME, feed.stime);
            values.put(KEY_FEED_UID, feed.uid);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_FEEDS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add quote to database");
        } finally {
            db.endTransaction();
        }
    }

    // Get all posts in the database
    public List<FeedModel> getAllFeeds() {
        List<FeedModel> feedlist = new ArrayList<>();

        String QUOTES_SELECT_QUERY = String.format("SELECT * FROM %s;", TABLE_FEEDS);

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(QUOTES_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    FeedModel newFeed = new FeedModel();
                    newFeed.key = cursor.getInt(cursor.getColumnIndex("id"));
                    newFeed.uname = cursor.getString(cursor.getColumnIndex(KEY_FEED_NAME));
                    newFeed.phoneno = cursor.getString(cursor.getColumnIndex(KEY_FEED_PHONE));
                    newFeed.stime = cursor.getString(cursor.getColumnIndex(KEY_FEED_STIME));
                    newFeed.uid = cursor.getString(cursor.getColumnIndex(KEY_FEED_UID));

                    feedlist.add(newFeed);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Quotes from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return feedlist;
    }

    public void deleteFeed(int key){
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_FEEDS, "id='" + String.valueOf(key) + "'", null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete all posts and users");
        } finally {
            db.endTransaction();
        }

    }

    public int countSavedFeeds(){
        List<FeedModel> allfeeds = getAllFeeds();
        return allfeeds.size();
    }

    public int countSavedThreads(){
        List<ThreadModel> allthreads = getAllThreads();
        return allthreads.size();
    }
}
