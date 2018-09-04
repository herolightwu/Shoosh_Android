package cz.org.shoosh.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import cz.org.shoosh.R;

public class OtherActivity extends AppCompatActivity {

    ImageView iv_home_back;
    TextView tv_activity_title, tv_content;

    String mtitle;
    private int mtype = 1;

    LinearLayout ll_menu;
    ImageView iv_home_menu, iv_home_feed, iv_home_message, iv_home_search;
    TextView tv_menu_about, tv_menu_privacy, tv_menu_terms, tv_menu_faq, tv_menu_account, tv_menu_invite;

    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance().getReference();
        mtitle = getIntent().getStringExtra("title");
        mtype = getIntent().getIntExtra("type", 1);
        setContentView(R.layout.activity_other);

        initLayout();
        initMenus();
        initSetting();
    }

    private void initSetting(){
        if(mtype == 2){
            iv_home_search.setVisibility(View.INVISIBLE);
            iv_home_menu.setVisibility(View.INVISIBLE);
            iv_home_message.setVisibility(View.INVISIBLE);
            iv_home_feed.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    private void initLayout(){
        iv_home_back = (ImageView) findViewById(R.id.iv_home_back);
        iv_home_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tv_activity_title = (TextView) findViewById(R.id.tv_activity_title);
        tv_activity_title.setText(mtitle);

        tv_content = (TextView) findViewById(R.id.tv_content);

        if(mtitle.equals("FAQS")){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                tv_content.setText(Html.fromHtml("<font color=\"#840303\">How do I use Shoosh?</font><p>Simply sign up and a phone number into a search.</p>", Html.FROM_HTML_MODE_LEGACY));
            } else {
                tv_content.setText(Html.fromHtml("<font color=\"#840303\">How do I use Shoosh?</font><p>Simply sign up and a phone number into a search.</p>"));
            }
            readFAQsFromDB();
        } else{
            /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                tv_content.setText(Html.fromHtml("<font color=\"#840303\">Title 1</font><p>Paragraph 1</p>", Html.FROM_HTML_MODE_LEGACY));
            } else {
                tv_content.setText(Html.fromHtml("<font color=\"#840303\">Title 1</font><p>Paragraph 1</p>"));
            }*/

            readContentFromDB();
        }
    }

    private void readContentFromDB(){

        String mainkey = "terms-conditions";
        if(mtitle.equals("PRIVACY")) mainkey = "privacy";

        database.child(mainkey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String mContent="";
                for(DataSnapshot para : dataSnapshot.getChildren()){
                    String t_cont = (String) para.child("content").getValue();
                    String t_title = (String) para.child("title").getValue();
                    mContent = mContent + "<font color=\"#840303\">" + t_title + "</font><p>" + t_cont + "</p> <br>";
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    tv_content.setText(Html.fromHtml(mContent, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    tv_content.setText(Html.fromHtml(mContent));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readFAQsFromDB(){

        database.child("faqs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String mFaqsString="";
                DataSnapshot topques = dataSnapshot.child("topques");
                for(DataSnapshot top_q : topques.getChildren()){
                    String t_ans = (String) top_q.child("ans").getValue();
                    String t_que = (String) top_q.child("ques").getValue();
                    mFaqsString = mFaqsString + "<font color=\"#840303\">" + t_que + "</font><p>" + t_ans + "</p> <br>";
                }
                mFaqsString = mFaqsString + "<h3><font color=\"blue\">Other Questions</font></h3><br>";
                DataSnapshot otherques = dataSnapshot.child("otherques");
                for(DataSnapshot other_q : otherques.getChildren()){
                    String o_ans = (String) other_q.child("ans").getValue();
                    String o_que = (String) other_q.child("ques").getValue();
                    mFaqsString = mFaqsString + "<font color=\"#840303\">" + o_que + "</font><p>" + o_ans + "</p> <br>";
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    tv_content.setText(Html.fromHtml(mFaqsString, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    tv_content.setText(Html.fromHtml(mFaqsString));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
                Intent intent = new Intent(OtherActivity.this, FeedActivity.class);
                startActivity(intent);
            }
        });

        iv_home_message = (ImageView) findViewById(R.id.iv_home_message);
        iv_home_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(OtherActivity.this, ThreadActivity.class);
                startActivity(intent);
            }
        });

        iv_home_search = (ImageView) findViewById(R.id.iv_home_search);
        iv_home_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OtherActivity.this, HomeActivity.class);
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
                Intent intent = new Intent(OtherActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        tv_menu_privacy = (TextView) findViewById(R.id.tv_menu_privacy);
        tv_menu_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                mtitle = "PRIVACY";
                initLayout();
            }
        });

        tv_menu_terms = (TextView) findViewById(R.id.tv_menu_terms);
        tv_menu_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                mtitle = "TERMS & CONDITIONS";
                initLayout();
            }
        });

        tv_menu_faq = (TextView) findViewById(R.id.tv_menu_faq);
        tv_menu_faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                mtitle = "FAQS";
                initLayout();
            }
        });

        tv_menu_account = (TextView) findViewById(R.id.tv_menu_account);
        tv_menu_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(OtherActivity.this, AccountActivity.class);
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
}
