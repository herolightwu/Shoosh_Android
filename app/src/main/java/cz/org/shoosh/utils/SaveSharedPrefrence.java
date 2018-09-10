package cz.org.shoosh.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SaveSharedPrefrence {
    Context context;
    SharedPreferences sharedPreferences;

    public static final String PREFS_NAME = "Shoosh";
    public static final String KEY_USER_ID = "key_uid";
    public static final String KEY_CONTACT_HIDE = "key_contacthide";
    public static final String KEY_NOTIFICATION = "key_notification";
    public static final String KEY_PHONENUMBER = "key_phonenumber";
    public static final String KEY_ALARMTIME = "key_alarmtime";

    public void saveKeyAlarmTime(Context context, long atime){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_ALARMTIME, atime);

        editor.commit();
    }

    public long getKeyAlarmTime(Context context){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long status = sharedPreferences.getLong(KEY_ALARMTIME, 0);

        return status;
    }

    public void saveKeyUserID(Context context, String userid){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userid);

        editor.commit();
    }

    public String getKeyUserID(Context context){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String status = sharedPreferences.getString(KEY_USER_ID, "0");

        return status;
    }

    public void saveKeyPhone(Context context, String sphone){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PHONENUMBER, sphone);

        editor.commit();
    }

    public String getKeyPhone(Context context){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String status = sharedPreferences.getString(KEY_PHONENUMBER, "0");

        return status;
    }

    public void saveKeyContact(Context context, String cont_str){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CONTACT_HIDE, cont_str);

        editor.commit();
    }

    public String getKeyContact(Context context){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String status = sharedPreferences.getString(KEY_CONTACT_HIDE, "2");

        return status;
    }

    public void saveKeyNotification(Context context, String userid){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_NOTIFICATION, userid);

        editor.commit();
    }

    public String getKeyNotification(Context context){
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String status = sharedPreferences.getString(KEY_NOTIFICATION, "2");

        return status;
    }

    public void DeletePrefrence(Context context) {

        sharedPreferences = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();

    }
}
