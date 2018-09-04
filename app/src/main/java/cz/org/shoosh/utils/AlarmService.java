package cz.org.shoosh.utils;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import cz.org.shoosh.R;

public class AlarmService extends IntentService {
    private SaveSharedPrefrence sharedPreferences;

    public AlarmService()
    {
        super("AlarmService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        long now_time = System.currentTimeMillis()/1000;
        sharedPreferences = new SaveSharedPrefrence();

        long alarmtime = sharedPreferences.getKeyAlarmTime(getApplicationContext());
        if(alarmtime == 0){
            sharedPreferences.saveKeyAlarmTime(getApplicationContext(), now_time);
        } else {

            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this, "notify_001");
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, myAppLinkToMarket, 0);

            NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
            bigText.bigText("Please rate us on PlayStore.");
            bigText.setBigContentTitle("Rate Us");
            bigText.setSummaryText("Alarm");

            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
            mBuilder.setContentTitle("Rate Us");
            mBuilder.setContentText("Please rate us on PlayStore.");
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

            mNotificationManager.notify(0, mBuilder.build());
            sharedPreferences.saveKeyAlarmTime(getApplicationContext(), now_time);
        }

    }


}
