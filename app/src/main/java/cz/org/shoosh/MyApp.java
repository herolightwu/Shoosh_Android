package cz.org.shoosh;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;
import com.ybs.countrypicker.Country;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.org.shoosh.activities.FeedActivity;
import cz.org.shoosh.activities.HomeActivity;
import cz.org.shoosh.activities.ThreadActivity;
import cz.org.shoosh.helpers.MySQLiteHelper;
import cz.org.shoosh.models.ContactModel;
import cz.org.shoosh.models.UserModel;
import cz.org.shoosh.utils.SaveSharedPrefrence;

public class MyApp extends Application {

    public static MyApp myApp = null;
    public static UserModel myProfile;
    public static UserModel otherUser;
    public static Boolean bContact, bNoti;
    public static Boolean bPaid;
    //public static int nComments;
    public static List<String> viewedComments;
    public static String topTitle;
    public static int feedBadge, threadBadge;
    public static List<UserModel> allusers;

    private MySQLiteHelper mHelper;
    public static List<ContactModel> allContacts;
    private String receiver_name="";
    private String roomID = "";
    public SaveSharedPrefrence sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        OneSignal.startInit(this)
                .autoPromptLocation(false) // default call promptLocation later
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .setNotificationReceivedHandler(new myNotificationReceivedHandler())
                .setNotificationOpenedHandler(new myNotificationOpenedHandler())
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

    }

    public static MyApp getInstance(){
        if(myApp == null)
        {
            myApp = new MyApp();
            myProfile = new UserModel();
            otherUser = new UserModel();
            allusers = new ArrayList<UserModel>();
            allContacts = new ArrayList<ContactModel>();
            bNoti = false;
            bContact = false;
            bPaid = false;
            topTitle = "";
            feedBadge = 0;
            threadBadge = 0;
        }
        return myApp;
    }

    public static MyApp getApp(){
        return myApp;
    }

    public static String parseContact(String contact, String countrycode) {
        Phonenumber.PhoneNumber phoneNumber = null;
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        String finalNumber = null;
        //String isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(countrycode));
        boolean isValid = false;
        PhoneNumberUtil.PhoneNumberType isMobile = null;
        try {
            phoneNumber = phoneNumberUtil.parse(contact, countrycode);
            isValid = phoneNumberUtil.isValidNumber(phoneNumber);
            isMobile = phoneNumberUtil.getNumberType(phoneNumber);

        } catch (NumberParseException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }


        if (isValid
                && (PhoneNumberUtil.PhoneNumberType.MOBILE == isMobile || PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE == isMobile)) {
            finalNumber = phoneNumberUtil.format(phoneNumber,
                    PhoneNumberUtil.PhoneNumberFormat.E164);
        }

        return finalNumber;
    }

    private class myNotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {
        @Override
        public void notificationReceived(OSNotification notification) {
            Intent intent;
            sharedPreferences = new SaveSharedPrefrence();
            JSONObject oneSignalData = notification.payload.additionalData;
            try{
                if(oneSignalData.has("type")){
                    String type = oneSignalData.getString("type");
                    String to_phone = oneSignalData.getString("tophone");
                    if(MyApp.getApp() == null){
                        //intent = new Intent(getApplicationContext(), MainActivity.class);
                    } else if(type.equals("comment")){
                        MyApp.getInstance().feedBadge = 1;
                    } else if(type.equals("thread")){
                        MyApp.getInstance().threadBadge = 1;
                    } else{
                        //intent = new Intent(getApplicationContext(), HomeActivity.class);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    }

                    //if(MyApp.getApp() == null) return;
                    //String bValue = sharedPreferences.getKeyNotification(getApplicationContext());
                    //if((type.equals("comment") || type.equals("thread")) && (bValue.equals("1")|| bValue.equals("2")))
                    //    getApplicationContext().startActivity(intent);
                }
            } catch (JSONException ex){

            }
        }
    }

    private class myNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
        // This fires when a notification is opened by tapping on it.
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            Intent intent;
            sharedPreferences = new SaveSharedPrefrence();
            JSONObject oneSignalData = result.notification.payload.additionalData;
            try{
                if(oneSignalData.has("type")){
                    String type = oneSignalData.getString("type");
                    if(MyApp.getApp() == null){
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                    } else if(type.equals("comment")){
                        if(MyApp.getInstance().topTitle.equals("Feed")){
                            intent = new Intent(getApplicationContext(), FeedActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        } else{
                            MyApp.getInstance().feedBadge = 1;
                            intent = new Intent(getApplicationContext(), FeedActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        }
                    } else if(type.equals("thread")){
                        if(MyApp.getInstance().topTitle.equals("Thread")){
                            intent = new Intent(getApplicationContext(), ThreadActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        } else if(MyApp.getInstance().topTitle.equals("Chat") && roomID.length() > 2){
                            MyApp.getInstance().threadBadge = 1;
                            intent = new Intent(getApplicationContext(), ThreadActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        } else{
                            MyApp.getInstance().threadBadge = 1;
                            intent = new Intent(getApplicationContext(), ThreadActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        }
                    } else{
                        intent = new Intent(getApplicationContext(), HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    }

                    //String to_phone = oneSignalData.getString("tophone");
                    //String bValue = sharedPreferences.getKeyNotification(getApplicationContext());
                    if((type.equals("comment") || type.equals("thread")))// && (bValue.equals("1")|| bValue.equals("2")))
                        getApplicationContext().startActivity(intent);
                }
            } catch (JSONException ex){

            }
        }
    }
}