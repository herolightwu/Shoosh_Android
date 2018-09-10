package cz.org.shoosh.activities;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import cz.org.shoosh.MyApp;
import cz.org.shoosh.R;
import cz.org.shoosh.utils.SaveSharedPrefrence;

public class AccountActivity extends AppCompatActivity {

    ImageView iv_home_back;

    LinearLayout ll_menu;
    ImageView iv_home_menu, iv_home_feed, iv_home_message, iv_home_search;
    TextView tv_menu_about, tv_menu_privacy, tv_menu_terms, tv_menu_faq, tv_menu_account, tv_menu_invite;

    EditText et_reg_value;
    Button btn_next;

    DatabaseReference database;
    SwitchCompat sw_contact, sw_notification;
    private SaveSharedPrefrence sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_account);
        sharedPreferences = new SaveSharedPrefrence();

        initLayout();
        initMenus();
    }

    private void initLayout(){
        iv_home_back = (ImageView) findViewById(R.id.iv_home_back);
        iv_home_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        et_reg_value = (EditText) findViewById(R.id.et_reg_value);
        btn_next = (Button) findViewById(R.id.btn_next);
        sw_contact = (SwitchCompat) findViewById(R.id.sw_contact);
        sw_notification = (SwitchCompat) findViewById(R.id.sw_notification);

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(et_reg_value.getText().toString().trim().length() > 0){
                    String name_str = et_reg_value.getText().toString();
                    String myID = MyApp.getInstance().myProfile.uid;
                    database.child("users").child(myID).child("userinfo").child("uname").setValue(name_str);
                    MyApp.getInstance().myProfile.uname = name_str;
                    et_reg_value.setText("");
                    et_reg_value.setHint(MyApp.getInstance().myProfile.uname);
                    showPopupDialog();
                }
            }
        });

        sw_notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String uid = MyApp.getInstance().myProfile.uid;
                if(b){
                    sharedPreferences.saveKeyNotification(getApplicationContext(), "1");
                    database.child("users").child(uid).child("userinfo").child("noti_set").setValue("1");
                } else{
                    sharedPreferences.saveKeyNotification(getApplicationContext(), "0");
                    database.child("users").child(uid).child("userinfo").child("noti_set").setValue("0");
                }
                MyApp.getInstance().bNoti = b;
                MyApp.getInstance().myProfile.bNoti = b;
            }
        });

        sw_contact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                MyApp.getInstance().bContact = b;
                MyApp.getInstance().myProfile.bHide = b;
                String uid = MyApp.getInstance().myProfile.uid;
                if(b){
                    sharedPreferences.saveKeyContact(getApplicationContext(), "1");
                    database.child("users").child(uid).child("userinfo").child("hideme").setValue("1");
                } else{
                    sharedPreferences.saveKeyContact(getApplicationContext(), "0");
                    database.child("users").child(uid).child("userinfo").child("hideme").setValue("0");
                }
            }
        });

        if(MyApp.getInstance().myProfile.bNoti){
            sw_notification.setChecked(true);
        }

        if(MyApp.getInstance().myProfile.bHide){
            sw_contact.setChecked(true);
        }

        et_reg_value.setHint(MyApp.getInstance().myProfile.uname);
    }

    public void showPopupDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.popup_rate_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);

        final TextView tv_content = (TextView) dialog.findViewById(R.id.tv_popup_content);
        String stxt = "Username successfully changed.";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tv_content.setText(Html.fromHtml(stxt, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tv_content.setText(Html.fromHtml(stxt));
        }

        TextView txt_OK = (TextView) dialog.findViewById(R.id.txt_OK);
        txt_OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void initMenus(){
        ll_menu = (LinearLayout) findViewById(R.id.ll_menu);
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
            }
        });

        iv_home_feed = (ImageView) findViewById(R.id.iv_home_feed);
        iv_home_feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(AccountActivity.this, FeedActivity.class);
                startActivity(intent);
            }
        });

        iv_home_message = (ImageView) findViewById(R.id.iv_home_message);
        iv_home_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(AccountActivity.this, ThreadActivity.class);
                startActivity(intent);
            }
        });

        iv_home_search = (ImageView) findViewById(R.id.iv_home_search);
        iv_home_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                //finish();
            }
        });

        tv_menu_about = (TextView) findViewById(R.id.tv_menu_about);
        tv_menu_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(AccountActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        tv_menu_privacy = (TextView) findViewById(R.id.tv_menu_privacy);
        tv_menu_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(AccountActivity.this, OtherActivity.class);
                intent.putExtra("title", "PRIVACY");
                startActivity(intent);
            }
        });

        tv_menu_terms = (TextView) findViewById(R.id.tv_menu_terms);
        tv_menu_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(AccountActivity.this, OtherActivity.class);
                intent.putExtra("title", "TERMS & CONDITIONS");
                startActivity(intent);
            }
        });

        tv_menu_faq = (TextView) findViewById(R.id.tv_menu_faq);
        tv_menu_faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(AccountActivity.this, OtherActivity.class);
                intent.putExtra("title", "FAQS");
                startActivity(intent);
            }
        });

        tv_menu_account = (TextView) findViewById(R.id.tv_menu_account);
        tv_menu_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();

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
}
