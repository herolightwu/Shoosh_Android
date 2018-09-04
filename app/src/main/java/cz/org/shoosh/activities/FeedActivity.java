package cz.org.shoosh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.ArrayList;
import java.util.List;

import cz.org.shoosh.MyApp;
import cz.org.shoosh.R;
import cz.org.shoosh.adapters.FeedListAdapter;
import cz.org.shoosh.helpers.MySQLiteHelper;
import cz.org.shoosh.models.FeedModel;

public class FeedActivity extends AppCompatActivity {
    private final String Acivity_Tag = "Feed";

    RecyclerView recyclerView;
    ImageView iv_home_back;
    List<FeedModel> feedlist;
    FeedListAdapter feedListAdapter;

    LinearLayout ll_menu;
    ImageView iv_home_menu, iv_home_feed, iv_home_message, iv_home_search;
    TextView tv_menu_about, tv_menu_privacy, tv_menu_terms, tv_menu_faq, tv_menu_account, tv_menu_invite;
    TextView tv_feed_nomail;

    private MySQLiteHelper mHelper;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_feed);
        mHelper = MySQLiteHelper.getInstance(this);

        initLayout();
        initMenus();
        getFeedsHistory();
    }

    @Override
    public void onResume(){
        super.onResume();
        MyApp.getInstance().topTitle = Acivity_Tag;
        getFeedsHistory();
    }

    @Override
    public void onStop(){
        super.onStop();
        MyApp.getInstance().topTitle = "";
    }

    private void initLayout(){
        iv_home_back = (ImageView) findViewById(R.id.iv_home_back);
        iv_home_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        feedlist = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        feedListAdapter = new FeedListAdapter(feedlist, getApplicationContext());
        recyclerView.setAdapter(feedListAdapter);

        feedListAdapter.setOnItemClickListener(new FeedListAdapter.OnItemClickListener() {
            @Override
            public int onItemClick(int position) {
                FeedModel one = feedlist.get(position);
                //mHelper.deleteFeed(one.key);
                String uid = MyApp.getInstance().myProfile.uid;
                database.child("users").child(uid).child("feeds").child(one.skey).setValue(null);
                Intent intent = new Intent(FeedActivity.this, ResultActivity.class);
                intent.putExtra("search_number", one.phoneno);
                startActivity(intent);
                return position;
            }
        });
        tv_feed_nomail = (TextView) findViewById(R.id.tv_feed_nomail);
    }

    private void getFeedsHistory(){

        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel("Waiting...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        String uid = MyApp.getInstance().myProfile.uid;
        database.child("users").child(uid).child("feeds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hud.dismiss();
                feedlist = new ArrayList<>();
                for(DataSnapshot sh : dataSnapshot.getChildren()){
                    String key = sh.getKey();
                    FeedModel one = new FeedModel();
                    one.skey = key;
                    one.uid = (String)sh.child("toid").getValue();
                    one.uname = (String)sh.child("toname").getValue();
                    one.phoneno = (String)sh.child("tophone").getValue();
                    one.stime = (String)sh.child("totime").getValue();
                    feedlist.add(one);
                }
                if(feedlist.size()>0){
                    recyclerView.setVisibility(View.VISIBLE);
                    tv_feed_nomail.setVisibility(View.GONE);
                } else{
                    recyclerView.setVisibility(View.GONE);
                    tv_feed_nomail.setVisibility(View.VISIBLE);
                }
                feedListAdapter.setDataList(feedlist);
                feedListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hud.dismiss();
            }
        });
        /*feedlist = mHelper.getAllFeeds();
        if(feedlist.size()>0){
            recyclerView.setVisibility(View.VISIBLE);
            tv_feed_nomail.setVisibility(View.GONE);
        } else{
            recyclerView.setVisibility(View.GONE);
            tv_feed_nomail.setVisibility(View.VISIBLE);
        }
        feedListAdapter.setDataList(feedlist);
        feedListAdapter.notifyDataSetChanged();*/
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

            }
        });

        iv_home_message = (ImageView) findViewById(R.id.iv_home_message);
        iv_home_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(FeedActivity.this, ThreadActivity.class);
                startActivity(intent);
            }
        });

        iv_home_search = (ImageView) findViewById(R.id.iv_home_search);
        iv_home_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FeedActivity.this, HomeActivity.class);
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
                Intent intent = new Intent(FeedActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        tv_menu_privacy = (TextView) findViewById(R.id.tv_menu_privacy);
        tv_menu_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(FeedActivity.this, OtherActivity.class);
                intent.putExtra("title", "PRIVACY");
                startActivity(intent);
            }
        });

        tv_menu_terms = (TextView) findViewById(R.id.tv_menu_terms);
        tv_menu_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(FeedActivity.this, OtherActivity.class);
                intent.putExtra("title", "TERMS & CONDITIONS");
                startActivity(intent);
            }
        });

        tv_menu_faq = (TextView) findViewById(R.id.tv_menu_faq);
        tv_menu_faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(FeedActivity.this, OtherActivity.class);
                intent.putExtra("title", "FAQS");
                startActivity(intent);
            }
        });

        tv_menu_account = (TextView) findViewById(R.id.tv_menu_account);
        tv_menu_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(FeedActivity.this, AccountActivity.class);
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
