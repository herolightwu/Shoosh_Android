package cz.org.shoosh.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.org.shoosh.MainActivity;
import cz.org.shoosh.MyApp;
import cz.org.shoosh.R;
import cz.org.shoosh.activities.ChatActivity;
import cz.org.shoosh.activities.FeedActivity;
import cz.org.shoosh.activities.HomeActivity;
import cz.org.shoosh.activities.ThreadActivity;
import cz.org.shoosh.helpers.MySQLiteHelper;
import cz.org.shoosh.models.ContactModel;
import cz.org.shoosh.models.FeedModel;
import cz.org.shoosh.models.ThreadModel;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String TAG = "Shoosh";

    private static int noti_index = 1;
    private String roomID = "";
    DatabaseReference database;
    SaveSharedPrefrence sharedPreferences;
    private MySQLiteHelper mHelper;

    List<ContactModel> contactslist;
    private String receiver_name="";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        /*sharedPreferences = new SaveSharedPrefrence();
        database = FirebaseDatabase.getInstance().getReference();
        final String refToken = s;
        final String uid = sharedPreferences.getKeyUserID(getApplicationContext());//MyApp.getInstance().myProfile.uid;
        database.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    database.child("users").child(uid).child("userinfo").child("token").setValue(refToken);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        MyApp.getInstance().myProfile.token = s;*/
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        String type = "";
        roomID = "";
        if (remoteMessage.getData().size() > 0) {
            //Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            mHelper = MySQLiteHelper.getInstance(this);
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();

            Map<String, String> data = remoteMessage.getData();
            type = data.get("type");
            String title = data.get("title");
            String body = data.get("body");
            if(type.equals("comment")){
                String to_phone = data.get("tophone");
                String to_name = data.get("toname");
                String to_uid = data.get("touser");
                if(checkValidatePhone(to_phone)){
                    FeedModel one = new FeedModel();
                    one.uname = receiver_name;
                    if(to_uid != null){
                        one.uid = to_uid;
                    } else{
                        one.uid = "";
                    }
                    one.phoneno = to_phone;
                    one.stime = ts;
                    mHelper.addFeed(one);
                    body = receiver_name + " has just received a comment.";
                    sendNotification(type, title, body);
                }
            } else if(type.equals("thread")){
                roomID = data.get("roomID");
                ThreadModel thread = new ThreadModel();
                thread.sid = data.get("send_id");
                thread.sname = data.get("send_name");
                thread.sphone = data.get("send_phone");
                thread.msg = data.get("msg");
                thread.stime = ts;
                mHelper.addThread(thread);
                sendNotification(type, title, body);
            }

            /*if(MyApp.getApp() != null){
                String myph= MyApp.getInstance().myProfile.phoneno;
                if(myph.equals(to_phone)){
                    sendNotification(type, title, "New comment was arrived");
                }
            }*/
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            //Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            /*String body = remoteMessage.getNotification().getBody();
            String title = remoteMessage.getNotification().getTitle();
            Map<String, String> data = remoteMessage.getData();
            if(type.length() > 1)
                sendNotification(type, title, body);*/

        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void sendNotification(String type, String title, String desc){
        Intent intent;
        Boolean bNew = false;
        if(MyApp.getApp() == null){
            intent = new Intent(this, MainActivity.class);
            bNew = true;
        } else{
            if(type.equals("comment")){
                if(MyApp.getInstance().topTitle.equals("Feed")){
                    intent = new Intent(this, FeedActivity.class);
                } else{
                    MyApp.getInstance().feedBadge = 1;
                    intent = new Intent(this, HomeActivity.class);
                    bNew = true;
                }
            } else if(type.equals("thread")){
                if(MyApp.getInstance().topTitle.equals("Thread")){
                    intent = new Intent(this, ThreadActivity.class);
                } else if(MyApp.getInstance().topTitle.equals("Chat") && roomID.length() > 2){
                    MyApp.getInstance().threadBadge = 1;
                    //intent = new Intent(this, ChatActivity.class);
                    //intent.putExtra("room_key", roomID);
                    intent = new Intent(this, HomeActivity.class);
                    bNew = true;
                } else{
                    MyApp.getInstance().threadBadge = 1;
                    intent = new Intent(this, HomeActivity.class);
                    bNew = true;
                }
            } else{
                intent = new Intent(this, HomeActivity.class);
                bNew = true;
            }
        }

        if(bNew)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        /*String name = title;
        String id = "Shoosh_channel_1"; // The user-visible name of the channel.
        String description = "Shoosh_first_channel"; // The user-visible description of the channel.

        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;
        final NotificationManager notifManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, name, importance);
                mChannel.setDescription(description);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[] { 500, 500, 1000, 1000, 1000 });
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(this, id);

            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            builder.setContentTitle(title)  // required
                    .setSmallIcon(R.mipmap.ic_launcher) // required
                    .setContentText(desc)  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(title)
                    .setVibrate(new long[] { 500, 500, 1000, 1000, 1000 });
        } else {

            builder = new NotificationCompat.Builder(this);

            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            builder.setContentTitle(title)                           // required
                    .setSmallIcon(R.mipmap.ic_launcher) // required
                    .setContentText(desc)  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(title)
                    .setVibrate(new long[] { 500, 500, 1000, 1000, 1000 })
                    .setPriority(Notification.PRIORITY_HIGH);
        } // else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        if(MyApp.getApp() == null ||(MyApp.getInstance().bNoti && type.equals("comment")) || (MyApp.getInstance().bContact && type.equals("thread"))){
            Notification notification = builder.build();
            notifManager.notify(noti_index, notification);
            //-----------------------------------------------------------------
            noti_index++ ; if(noti_index == Integer.MAX_VALUE) noti_index = 1;
        }*/

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, "notify_001");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(desc);
        bigText.setBigContentTitle(title);
        bigText.setSummaryText(type);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(desc);
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

        if(MyApp.getApp() == null ||(MyApp.getInstance().bNoti && type.equals("comment")) || type.equals("thread"))
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
