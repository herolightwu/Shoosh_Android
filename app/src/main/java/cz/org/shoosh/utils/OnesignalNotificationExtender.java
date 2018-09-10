package cz.org.shoosh.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationDisplayedResult;
import com.onesignal.OSNotificationReceivedResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.org.shoosh.MyApp;
import cz.org.shoosh.R;
import cz.org.shoosh.helpers.MySQLiteHelper;
import cz.org.shoosh.models.ContactModel;
import cz.org.shoosh.models.FeedModel;
import cz.org.shoosh.models.ThreadModel;

public class OnesignalNotificationExtender extends NotificationExtenderService {

    private String roomID = "";
    private MySQLiteHelper mHelper;

    List<ContactModel> contactslist;
    private String receiver_name="";
    public SaveSharedPrefrence sharedPreferences;

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {

        final OSNotificationReceivedResult receiveData = receivedResult;
        sharedPreferences = new SaveSharedPrefrence();
        // Read Properties from result
        OverrideSettings overrideSettings = new OverrideSettings();
        overrideSettings.extender = new NotificationCompat.Extender() {
            @Override
            public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {
                // Sets the background notification color to Red on Android 5.0+ devices.
                try{
                    JSONObject oneData = receiveData.payload.additionalData;
                    if (!oneData.has("type")) {
                        return null;
                    }
                    String type = oneData.getString("type");
                    String body = receiveData.payload.body;//receiver_name + " has just received a comment.";
                    //if(type.equals("thread")){
                    //    body = receiveData.payload.body;
                    //}
                    NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
                    bigText.bigText(body);
                    bigText.setBigContentTitle("Shoosh");
                    bigText.setSummaryText(type);

                    //mBuilder.setContentIntent(pendingIntent);
                    builder.setSmallIcon(R.mipmap.ic_launcher_round);
                    builder.setContentTitle("Shoosh");
                    builder.setContentText(body);
                    builder.setPriority(Notification.PRIORITY_MAX);
                    builder.setStyle(bigText);
                    builder.setDefaults(Notification.DEFAULT_ALL);
                    builder.setAutoCancel(true);
                    builder.setVibrate(new long[] { 500, 500, 1000, 1000, 1000 });

                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        builder.setChannelId("notify_001");
                        NotificationChannel channel = new NotificationChannel("notify_001",
                                "Channel human readable title",
                                NotificationManager.IMPORTANCE_DEFAULT);
                        mNotificationManager.createNotificationChannel(channel);
                    }

                    //String bValue = sharedPreferences.getKeyNotification(getApplicationContext());
                    if((type.equals("comment") || type.equals("thread")))// && (bValue.equals("1")|| bValue.equals("2")))
                        return builder;
                } catch (JSONException ex){
                    return null;
                }
                return null;
            }
        };

        mHelper = MySQLiteHelper.getInstance(getApplicationContext());
        JSONObject oneSignalData = receivedResult.payload.additionalData;

        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        try{
            /*--- check Onesignal notification.-------*/
            if (!oneSignalData.has("type")) {
                return true;
            }
            /*----------------------------------------*/
            String type = oneSignalData.getString("type");
            if(type.equals("comment")){
                String to_phone = oneSignalData.getString("tophone");
                String to_name = oneSignalData.getString("toname");
                String to_uid = oneSignalData.getString("touser");
                //if(checkValidatePhone(to_phone)){
                    FeedModel one = new FeedModel();
                    one.uname = to_name;//receiver_name;
                    if(to_uid != null){
                        one.uid = to_uid;
                    } else{
                        one.uid = "";
                    }
                    one.phoneno = to_phone;
                    one.stime = ts;
                    mHelper.addFeed(one);
                  OSNotificationDisplayedResult displayedResult = displayNotification(overrideSettings);
                //displayNotification(receiveData.payload.body, type);
                    return true;
                //}
            } else if(type.equals("thread")){
                roomID = oneSignalData.getString("roomID");
                ThreadModel thread = new ThreadModel();
                thread.sid = oneSignalData.getString("send_id");
                thread.sname = oneSignalData.getString("send_name");
                thread.sphone = oneSignalData.getString("send_phone");
                thread.msg = oneSignalData.getString("msg");
                thread.stime = ts;
                mHelper.addThread(thread);
                OSNotificationDisplayedResult displayedResult = displayNotification(overrideSettings);
                //displayNotification(receiveData.payload.body, type);
                return true;
            }
        } catch (JSONException ex){

        }
        return true;
    }

    private void displayNotification(String body, String type){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, "notify_001");

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(body);
        bigText.setBigContentTitle("Shoosh");
        bigText.setSummaryText(type);

        //mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle("Shoosh");
        mBuilder.setContentText(body);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setAutoCancel(true);
        mBuilder.setVibrate(new long[] { 500, 500, 1000, 1000, 1000 });

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("notify_001",
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
        }

        String bValue = sharedPreferences.getKeyNotification(getApplicationContext());
        if((type.equals("comment") || type.equals("thread")) && ((bValue.equals("1")|| bValue.equals("2"))|| MyApp.getApp() == null))
            mNotificationManager.notify(0, mBuilder.build());
    }

    private boolean checkValidatePhone(String phoneno){
        getContactList();
        boolean bret = false;
        for(ContactModel one : contactslist){
            String temp = one.phone;
            String ph_no = temp.replace("-","");
            temp = ph_no.replace("(","");
            ph_no = temp.replace(")","");
            temp = ph_no.replace(" ", "");
            if(temp.equals(phoneno)){
                bret = true;//return true;
                receiver_name = one.name;
                break;
            }
        }
        return bret;
    }

    private void getContactList() {
        contactslist = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        if(cr == null) return;
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String photoStr = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_THUMBNAIL_URI));
                        ContactModel one = new ContactModel();
                        one.name = name;
                        one.phone = phoneNo;
                        one.photo = photoStr;
                        contactslist.add(one);
                        //Log.i(TAG, "Name: " + name);
                        //Log.i(TAG, "Phone Number: " + phoneNo);
                    }
                    pCur.close();
                }
            }
        }
        if(cur!=null){
            cur.close();
        }
    }

}
