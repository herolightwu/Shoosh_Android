package cz.org.shoosh.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;
import com.ybs.countrypicker.CountryPicker;
import com.ybs.countrypicker.CountryPickerListener;

import java.util.ArrayList;
import java.util.List;

import cz.org.shoosh.MyApp;
import cz.org.shoosh.R;
import cz.org.shoosh.helpers.MySQLiteHelper;
import cz.org.shoosh.models.ContactModel;
import cz.org.shoosh.models.UserModel;
import cz.org.shoosh.utils.SaveSharedPrefrence;


public class HomeActivity extends AppCompatActivity {

    TextView tv_enter_number, tv_add_number;
    RelativeLayout rl_enternumber;
    LinearLayout ll_menu;
    ImageView iv_home_goto, iv_home_menu, iv_home_feed, iv_home_message, iv_home_search;
    TextView tv_menu_about, tv_menu_privacy, tv_menu_terms, tv_menu_faq, tv_menu_account, tv_menu_invite;

    CountryPicker picker;
    TextView tv_reg_country_code;
    ImageView iv_reg_country_flag;
    LinearLayout ll_reg_country;
    EditText et_phonenumber;
    DatabaseReference database;
    String selectCode = "AU";

    TextView tv_feed_badge, tv_thread_badge;
    private static long preTime = 0;

    private SaveSharedPrefrence sharedPreferences;
    private MySQLiteHelper mHelper;
    public static final int HOME_PERMISSIONS_REQUEST= 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_home);

        initLayout();
        setFunctions();
        initMenus();
        initViewedCommentList();

        sharedPreferences = new SaveSharedPrefrence();
        long alarmtime = sharedPreferences.getKeyAlarmTime(getApplicationContext());
        if(alarmtime == 0){
            setAlarm();
        }

        mHelper = MySQLiteHelper.getInstance(this);
    }

    private void setAlarm(){

        long now_time = System.currentTimeMillis()/1000;
        long alarmtime = sharedPreferences.getKeyAlarmTime(getApplicationContext());
        if(alarmtime == 0){
            sharedPreferences.saveKeyAlarmTime(getApplicationContext(), now_time);
        }

        /*Calendar setcalendar = Calendar.getInstance();

        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, setcalendar.getTimeInMillis(),7 * 60 * 1000, pendingIntent);//24 * 60
        */

    }

    private void initLayout(){

        tv_feed_badge = (TextView) findViewById(R.id.tv_feed_badge);
        tv_thread_badge = (TextView) findViewById(R.id.tv_thread_badge);
        tv_enter_number = (TextView) findViewById(R.id.tv_enter_number);
        tv_add_number = (TextView) findViewById(R.id.tv_add_number);
        ll_reg_country = (LinearLayout) findViewById(R.id.ll_reg_country);
        tv_reg_country_code = (TextView) findViewById(R.id.tv_reg_country_code);
        iv_reg_country_flag = (ImageView) findViewById(R.id.iv_reg_country_flag);
        et_phonenumber = (EditText) findViewById(R.id.et_phonenumber);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tv_enter_number.setText(Html.fromHtml(" <b>Enter</b> a number<br>to begin a search", Html.FROM_HTML_MODE_LEGACY));
            tv_add_number.setText(Html.fromHtml("<b>Add</b> a number<br>from your contacts", Html.FROM_HTML_MODE_LEGACY));
        } else {
            tv_enter_number.setText(Html.fromHtml("<b>Enter</b> a number<br>to begin a search"));
            tv_add_number.setText(Html.fromHtml("<b>Add</b> a number<br>from your contacts"));
        }

        rl_enternumber = (RelativeLayout) findViewById(R.id.rl_enternumber);
        ll_menu = (LinearLayout) findViewById(R.id.ll_menu);

        iv_home_goto = (ImageView) findViewById(R.id.iv_home_goto);

        picker = CountryPicker.newInstance("Select Country");  // dialog title
        picker.setListener(new CountryPickerListener() {
            @Override
            public void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID) {

                tv_reg_country_code.setText(dialCode);
                selectCode = code.toUpperCase();
                iv_reg_country_flag.setImageResource(flagDrawableResID);
                picker.dismiss();

            }
        });

        ll_reg_country.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
            }
        });


    }

    private void initViewedCommentList(){
        String uid = MyApp.getInstance().myProfile.uid;
        database.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                MyApp.getInstance().viewedComments = new ArrayList<>();
                if(dataSnapshot.hasChild("viewedcomments")){
                    DataSnapshot vd_lists = dataSnapshot.child("viewedcomments");
                    for(DataSnapshot vdc : vd_lists.getChildren()){
                        String one = (String) vdc.getValue();
                        MyApp.getInstance().viewedComments.add(one);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAllUsers(){
        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel("Waiting...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        MyApp.getInstance().allusers = new ArrayList<>();
        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot user_shot : dataSnapshot.getChildren()){
                    DataSnapshot uinfo = user_shot.child("userinfo");
                    if(uinfo.hasChild("uname")){
                        UserModel one = new UserModel();
                        one.uname = (String)uinfo.child("uname").getValue();
                        one.phoneno = (String)uinfo.child("phoneno").getValue();
                        one.token = (String)uinfo.child("token").getValue();
                        one.devtype = (String)uinfo.child("devtype").getValue();
                        one.uid = user_shot.getKey();
                        one.bNoti = true;
                        one.bHide = false;
                        /*if(uinfo.hasChild("noti_set")){
                            String sNoti = (String) uinfo.child("noti_set").getValue();
                            if(sNoti.equals("0")){
                                one.bNoti = false;
                            }
                        }*/
                        if(uinfo.hasChild("hideme")){
                            String sNoti = (String) uinfo.child("hideme").getValue();
                            if(sNoti.equals("1")){
                                one.bHide = true;
                            }
                        }
                        one.contactlist = "";
                        DataSnapshot contact_sh = user_shot.child("contactlist");
                        for(DataSnapshot temp_sh : contact_sh.getChildren()){
                            String tt_str = (String) temp_sh.getValue();
                            if(one.contactlist.length() == 0){
                                one.contactlist = tt_str;
                            } else{
                                one.contactlist = one.contactlist + "," + tt_str;
                            }
                        }
                        MyApp.getInstance().allusers.add(one);
                    }
                }
                hud.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hud.dismiss();
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        Animation slideLeft = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.home_slide_left);
        Animation slideRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.home_slide_right);

        if(rl_enternumber.getVisibility()==View.VISIBLE) {
            rl_enternumber.startAnimation(slideRight);
            rl_enternumber.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        getContactList();
    }

    @Override
    public void onResume(){
        super.onResume();
        initToken();

        long now_time = System.currentTimeMillis()/1000;
        if(preTime == 0 || (now_time - preTime) > 36000){
            getAllUsers();
            preTime = now_time;
        }
        refreshBadges();
        long alarmtime = sharedPreferences.getKeyAlarmTime(getApplicationContext());
        long timegap = (now_time - alarmtime) / 3600;
        if(timegap >= 168){ // 7*24
            showPopupDialog();
            sharedPreferences.saveKeyAlarmTime(getApplicationContext(), now_time);
        }
    }

    private void refreshBadges(){
        String uid = MyApp.getInstance().myProfile.uid;
        database.child("users").child(uid).child("feeds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long feedcount = dataSnapshot.getChildrenCount();
                if(feedcount > 0){
                    tv_feed_badge.setVisibility(View.VISIBLE);
                    tv_feed_badge.setText(String.valueOf(feedcount));
                } else{
                    tv_feed_badge.setVisibility(View.GONE);
                    tv_feed_badge.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        database.child("users").child(uid).child("threads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                long threadcount = 0;
                for(DataSnapshot sh : dataSnapshot.getChildren()){
                    if(sh.hasChild("read")){
                        continue;
                    } else{
                        threadcount ++;
                    }
                }
                if(threadcount > 0){
                    tv_thread_badge.setVisibility(View.VISIBLE);
                    tv_thread_badge.setText(String.valueOf(threadcount));
                } else{
                    tv_thread_badge.setVisibility(View.GONE);
                    tv_thread_badge.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void showPopupDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.popup_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);

        final TextView tv_content = (TextView) dialog.findViewById(R.id.tv_popup_content);
        String stxt = "<b>Enjoying Shoosh?</b> <br> Please rate us.";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tv_content.setText(Html.fromHtml(stxt, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tv_content.setText(Html.fromHtml(stxt));
        }

        TextView txt_OK = (TextView) dialog.findViewById(R.id.txt_OK);
        txt_OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMarket();
                dialog.dismiss();
            }
        });
        TextView txt_cancel = (TextView) dialog.findViewById(R.id.txt_cancel);
        txt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void initMenus(){
        iv_home_menu = (ImageView) findViewById(R.id.iv_home_menu);
        iv_home_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.menu_slide_up);
                Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.menu_slide_down);

                if(ll_menu.getVisibility()==View.INVISIBLE) {
                    ll_menu.startAnimation(slideDown);
                    ll_menu.setVisibility(View.VISIBLE);
                } else{
                    ll_menu.startAnimation(slideUp);
                    ll_menu.setVisibility(View.INVISIBLE);
                }

                Animation slideLeft = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.home_slide_left);
                Animation slideRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.home_slide_right);

                if(rl_enternumber.getVisibility()==View.VISIBLE) {
                    rl_enternumber.startAnimation(slideRight);
                    rl_enternumber.setVisibility(View.INVISIBLE);
                }
            }
        });

        iv_home_feed = (ImageView) findViewById(R.id.iv_home_feed);
        iv_home_feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                MyApp.getInstance().feedBadge = 0;
                tv_feed_badge.setVisibility(View.GONE);
                Intent intent = new Intent(HomeActivity.this, FeedActivity.class);
                startActivity(intent);
            }
        });

        iv_home_message = (ImageView) findViewById(R.id.iv_home_message);
        iv_home_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                MyApp.getInstance().threadBadge = 0;
                tv_thread_badge.setVisibility(View.GONE);
                Intent intent = new Intent(HomeActivity.this, ThreadActivity.class);
                startActivity(intent);
            }
        });

        iv_home_search = (ImageView) findViewById(R.id.iv_home_search);

        tv_menu_about = (TextView) findViewById(R.id.tv_menu_about);
        tv_menu_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(HomeActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        tv_menu_privacy = (TextView) findViewById(R.id.tv_menu_privacy);
        tv_menu_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(HomeActivity.this, OtherActivity.class);
                intent.putExtra("title", "PRIVACY");
                startActivity(intent);
            }
        });

        tv_menu_terms = (TextView) findViewById(R.id.tv_menu_terms);
        tv_menu_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(HomeActivity.this, OtherActivity.class);
                intent.putExtra("title", "TERMS & CONDITIONS");
                startActivity(intent);
            }
        });

        tv_menu_faq = (TextView) findViewById(R.id.tv_menu_faq);
        tv_menu_faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(HomeActivity.this, OtherActivity.class);
                intent.putExtra("title", "FAQS");
                startActivity(intent);
            }
        });

        tv_menu_account = (TextView) findViewById(R.id.tv_menu_account);
        tv_menu_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(HomeActivity.this, AccountActivity.class);
                startActivity(intent);
            }
        });

        tv_menu_invite = (TextView) findViewById(R.id.tv_menu_invite);
        tv_menu_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                shareStoreInfo();
            }
        });
    }

    private void menuSlideUp(){
        Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.menu_slide_up);
        if(ll_menu.getVisibility() == View.VISIBLE){
            ll_menu.startAnimation(slideUp);
            ll_menu.setVisibility(View.INVISIBLE);
        }
    }

    private void shareStoreInfo(){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE); //shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Join Shoosh today and read reviews of people you know.\nGet it from your Android App Store.\nhttps://play.google.com/store/apps/details?id=cz.org.shoosh");
        //String post_str = "(" + selStore.storeAddr.address + " " +selStore.storeAddr.city + ", " + selStore.storeAddr.state + ", " + selStore.storeAddr.country + ")";
        //shareIntent.putExtra(Intent.EXTRA_TEXT, selStore.name + post_str);
        shareIntent.setType("text/*");
        startActivity(Intent.createChooser(shareIntent, "Invite A Friend"));
    }

    private void setFunctions(){
        tv_enter_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                menuSlideUp();

                et_phonenumber.setText("");
                et_phonenumber.setFocusable(true);

                Animation slideLeft = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.home_slide_left);
                Animation slideRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.home_slide_right);

                if(rl_enternumber.getVisibility()==View.INVISIBLE) {
                    rl_enternumber.startAnimation(slideLeft);
                    rl_enternumber.setVisibility(View.VISIBLE);
                }
            }
        });

        iv_home_goto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String search_number = tv_reg_country_code.getText().toString() + et_phonenumber.getText().toString().trim();
                if(search_number.length() > 4 ){//&& !search_number.equals(MyApp.getInstance().myProfile.phoneno)
                    String new_number = MyApp.parseContact(et_phonenumber.getText().toString().trim(), selectCode);
                    if(new_number != null){
                        Animation slideLeft = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.home_slide_left);
                        Animation slideRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.home_slide_right);

                        if(rl_enternumber.getVisibility()==View.VISIBLE) {
                            rl_enternumber.startAnimation(slideRight);
                            rl_enternumber.setVisibility(View.INVISIBLE);
                        }

                        Intent intent = new Intent(HomeActivity.this, ResultActivity.class);
                        intent.putExtra("search_number", new_number);
                        startActivity(intent);
                    } else{
                        Toast.makeText(HomeActivity.this, "Please type correct phone number.", Toast.LENGTH_SHORT).show();
                    }
                } else{
                    Toast.makeText(HomeActivity.this, "Please type correct phone number.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        tv_add_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(HomeActivity.this, ContactsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initToken(){
        /*FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                database = FirebaseDatabase.getInstance().getReference();
                String uid = MyApp.getInstance().myProfile.uid;
                database.child("users").child(uid).child("userinfo").child("token").setValue(newToken);
                MyApp.getInstance().myProfile.token = newToken;
            }
        });*/
        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        String oneSignalId = status.getSubscriptionStatus().getUserId();
        String uid = MyApp.getInstance().myProfile.uid;
        database.child("users").child(uid).child("userinfo").child("token").setValue(oneSignalId);
    }

    private void getContactList() {
        List<ContactModel> contactslist = new ArrayList<>();
        try{
            ContentResolver cr = getContentResolver();
            if(cr == null) return;
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");

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
                            String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String photoStr = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_THUMBNAIL_URI));
                            if(phoneNo.length() > 0 && name.length() > 0){
                                ContactModel one = new ContactModel();
                                one.name = name;
                                one.phone = phoneNo;
                                one.photo = photoStr;
                                contactslist.add(one);
                            }
                        }
                        pCur.close();
                    }
                }
            }
            if(cur!=null){
                cur.close();
            }
            MyApp.getInstance().allContacts = contactslist;
            int count = 0;
            String uid = MyApp.getInstance().myProfile.uid;
            database.child("users").child(uid).child("contactlist").setValue(null);
            String tempStr = "";
            for(ContactModel one : contactslist){
                String phoneno = one.phone.replace(" ", "");
                String tempstr = phoneno.replace("-","");
                phoneno = tempstr.replace("(","");
                tempstr = phoneno.replace(")","");
                if(count % 10 == 0){
                    tempStr = one.name + ":" + tempstr;
                } else{
                    tempStr = tempStr +"," + one.name + ":" + tempstr;
                }
                count++;
                if(count % 10 == 0 || (count == contactslist.size())){
                    String tempkey = database.child("users").child(uid).child("contactlist").push().getKey();
                    database.child("users").child(uid).child("contactlist").child(tempkey).setValue(tempStr);
                    tempStr = "";
                }
            }
        } catch (Exception e){
            int currentAPIVersion = Build.VERSION.SDK_INT;
            if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
            {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED) {

                    try {
                        ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.READ_CONTACTS}, HOME_PERMISSIONS_REQUEST);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    private void launchMarket() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getPackageName())));
        }
        catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case HOME_PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContactList();
                } else {
                    //code for deny
                }
                break;
        }
    }

}
