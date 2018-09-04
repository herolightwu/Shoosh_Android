package cz.org.shoosh.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import cz.org.shoosh.MyApp;
import cz.org.shoosh.R;
import cz.org.shoosh.adapters.ThreadListAdapter;
import cz.org.shoosh.helpers.MySQLiteHelper;
import cz.org.shoosh.models.ThreadModel;
import cz.org.shoosh.models.UserModel;

public class ThreadActivity extends AppCompatActivity{

    private final String Acivity_Tag = "Thread";

    RecyclerView recyclerView;
    ImageView iv_home_back;
    List<ThreadModel> threadlist;
    ThreadListAdapter threadListAdapter;

    LinearLayout ll_menu;
    ImageView iv_home_menu, iv_home_feed, iv_home_message, iv_home_search;
    TextView tv_menu_about, tv_menu_privacy, tv_menu_terms, tv_menu_faq, tv_menu_account, tv_menu_invite;
    TextView tv_thread_nomail;

    private MySQLiteHelper mHelper;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_thread);
        mHelper = MySQLiteHelper.getInstance(this);
        initLayout();
        initMenus();
        getThreadsHistory();

    }

    @Override
    public void onResume(){
        super.onResume();
        MyApp.getInstance().topTitle = Acivity_Tag;
        getThreadsHistory();
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

        threadlist = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        threadListAdapter = new ThreadListAdapter(threadlist, getApplicationContext());
        recyclerView.setAdapter(threadListAdapter);

        threadListAdapter.setOnItemClickListener(new ThreadListAdapter.OnItemClickListener() {
            @Override
            public int onItemClick(int position) {
                ThreadModel one = threadlist.get(position);
                if(one.sphone.equals("Shoosh")){
                    showReportDialog(position);
                } else if(one.sname.equals("Shoosh") && one.sphone.equals("Dispute")){
                    //delReportDialog(position);
                } else{
                    String uid = MyApp.getInstance().myProfile.uid;
                    database.child("users").child(uid).child("threads").child(one.skey).child("read").setValue("1");
                    String roomid = getMessageKey(one.sphone);
                    Intent intent = new Intent(ThreadActivity.this, ChatActivity.class);
                    intent.putExtra("room_key", roomid);
                    startActivity(intent);
                }

                return position;
            }
        });

        threadListAdapter.setOnOptionMenuListener(new ThreadListAdapter.OnOptionMenuListener() {
            @Override
            public int onOptionMenu(View view, int position) {
                showOptionMenu(view, position);
                return position;
            }
        });
        tv_thread_nomail = (TextView) findViewById(R.id.tv_thread_nomail);
    }

    private void showOptionMenu(View view, final int pos){
        final PopupMenu menu = new PopupMenu(this, view);
        menu.inflate(R.menu.thread_option_menu);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.menu_item_delete){
                    ThreadModel other = threadlist.get(pos);
                    String uid = MyApp.getInstance().myProfile.uid;
                    database.child("users").child(uid).child("threads").child(other.skey).setValue(null);
                    getThreadsHistory();
                    return true;
                }
                return false;
            }
        });

        try{
            Field field = menu.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            Object menuPopupHelper = field.get(menu);
            Class<?> cls = Class.forName("com.android.internal.view.menu.MenuPopupHelper");
            Method method = cls.getDeclaredMethod("setForceShowIcon", new Class[]{boolean.class});
            method.setAccessible(true);
            method.invoke(menuPopupHelper, new Object[]{true});
        } catch (Exception e){
            e.printStackTrace();
        }
        menu.show();
    }

    private void delReportDialog(final int pos) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.popup_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);

        final TextView tv_content = (TextView) dialog.findViewById(R.id.tv_popup_content);

        tv_content.setText("Do you want delete this message?");

        TextView txt_OK = (TextView) dialog.findViewById(R.id.txt_OK);
        txt_OK.setText("No");
        txt_OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        TextView txt_cancel = (TextView) dialog.findViewById(R.id.txt_cancel);
        txt_cancel.setText("Delete");
        txt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadModel one = threadlist.get(pos);
                String uid = MyApp.getInstance().myProfile.uid;
                database.child("users").child(uid).child("threads").child(one.skey).setValue(null);
                getThreadsHistory();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showReportDialog(final int pos) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.popup_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);

        final TextView tv_content = (TextView) dialog.findViewById(R.id.tv_popup_content);
        ThreadModel one = threadlist.get(pos);
        String[] ct_str = one.msg.split(":::");
        String content_str = one.sname + " has disputed your comment. " + getString(R.string.report_confirm_str);
        if(ct_str.length == 2){
            content_str = one.sname + " has disputed your comment \"" + ct_str[1] + "\". " + getString(R.string.report_confirm_str);
        }
        tv_content.setText(content_str);

        TextView txt_OK = (TextView) dialog.findViewById(R.id.txt_OK);
        txt_OK.setText("Delete");
        txt_OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteReportedComment(pos);
                dialog.dismiss();
            }
        });
        TextView txt_cancel = (TextView) dialog.findViewById(R.id.txt_cancel);
        txt_cancel.setText("Confirm");
        txt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmReportedComment(pos);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void confirmReportedComment(int pos){
        ThreadModel one = threadlist.get(pos);
        String sId = one.sid;
        String[] ct_str = one.msg.split(":::");
        String cKey = ct_str[0];
        List<UserModel> allusers = MyApp.getInstance().allusers;
        for(UserModel one_user : allusers){
            if(one_user.uid.equals(sId)){
                final UserModel toUser = one_user;
                database.child("comments").child(one_user.phoneno).child(cKey).child("report").setValue("0");
                database.child("users").child(MyApp.getInstance().myProfile.uid).child("threads").child(one.skey).setValue(null);
                String bodystr = "The comment has been confirmed by the author. This comment will remain.";
                if(ct_str.length == 2){
                    bodystr = "The comment \"" + ct_str[1] + "\" has been confirmed by the author. This comment will remain.";
                }
                final String body = bodystr;
                try {
                    OneSignal.postNotification(new JSONObject("{'include_player_ids': ['" + one_user.token + "'], " +
                                    "'contents': {'en':'" + body + "'}, " +
                                    "'ios_badgeType': 'SetTo', " +
                                    "'ios_badgeCount': 1, " +
                                    "'data': {'type': 'thread', 'roomID': '" + "report" + "' , 'send_id': '"+ MyApp.getInstance().myProfile.uid +"', 'send_name': '" + "Shoosh" + "' , 'send_phone': '"+ "Dispute" +"', 'msg': '" + body +"' }}"),
                            new OneSignal.PostNotificationResponseHandler() {
                                @Override
                                public void onSuccess(JSONObject response) {
                                    Long tsLong = System.currentTimeMillis()/1000;
                                    String ts = tsLong.toString();
                                    String kkey = database.child("users").child(toUser.uid).child("threads").push().getKey();
                                    database.child("users").child(toUser.uid).child("threads").child(kkey).child("toid").setValue(MyApp.getInstance().myProfile.uid);
                                    database.child("users").child(toUser.uid).child("threads").child(kkey).child("toname").setValue("Shoosh");
                                    database.child("users").child(toUser.uid).child("threads").child(kkey).child("tophone").setValue("Dispute");
                                    database.child("users").child(toUser.uid).child("threads").child(kkey).child("msg").setValue(body);
                                    database.child("users").child(toUser.uid).child("threads").child(kkey).child("totime").setValue(ts);
                                    database.child("users").child(toUser.uid).child("threads").child(kkey).child("read").setValue("1");
                                }
                                @Override
                                public void onFailure(JSONObject response) {
                                    Log.e("OneSignalExample", "postNotification Failure: " + response);
                                }
                            });
                    Toast.makeText(this, "The disputed comment has been confirmed by the author. This comment will remain.", Toast.LENGTH_SHORT).show();
                    getThreadsHistory();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void deleteReportedComment(int pos){
        ThreadModel one = threadlist.get(pos);
        String sId = one.sid;
        String[] ct_str = one.msg.split(":::");
        String cKey = ct_str[0];
        List<UserModel> allusers = MyApp.getInstance().allusers;
        for(UserModel one_user : allusers){
            if(one_user.uid.equals(sId)){
                final UserModel toUser = one_user;
                database.child("comments").child(one_user.phoneno).child(cKey).setValue(null);
                database.child("users").child(MyApp.getInstance().myProfile.uid).child("threads").child(one.skey).setValue(null);
                String bodystr = "The comment has been withdrawn by " + MyApp.getInstance().myProfile.uname;
                if(ct_str.length == 2){
                    bodystr = "The comment \"" + ct_str[1] + "\" has been withdrawn by " + MyApp.getInstance().myProfile.uname;
                }
                final String body = bodystr;
                try {
                    OneSignal.postNotification(new JSONObject("{'include_player_ids': ['" + one_user.token + "'], " +
                                    "'contents': {'en':'" + body + "'}, " +
                                    "'ios_badgeType': 'SetTo', " +
                                    "'ios_badgeCount': 1, " +
                                    "'data': {'type': 'thread', 'roomID': '" + "report" + "' , 'send_id': '"+ MyApp.getInstance().myProfile.uid +"', 'send_name': '" + "Shoosh" + "' , 'send_phone': '"+ "Dispute" +"', 'msg': '" + body +"' }}"),
                            new OneSignal.PostNotificationResponseHandler() {
                                @Override
                                public void onSuccess(JSONObject response) {
                                    Long tsLong = System.currentTimeMillis()/1000;
                                    String ts = tsLong.toString();
                                    String kkey = database.child("users").child(toUser.uid).child("threads").push().getKey();
                                    database.child("users").child(toUser.uid).child("threads").child(kkey).child("toid").setValue(MyApp.getInstance().myProfile.uid);
                                    database.child("users").child(toUser.uid).child("threads").child(kkey).child("toname").setValue("Shoosh");
                                    database.child("users").child(toUser.uid).child("threads").child(kkey).child("tophone").setValue("Dispute");
                                    database.child("users").child(toUser.uid).child("threads").child(kkey).child("msg").setValue(body);
                                    database.child("users").child(toUser.uid).child("threads").child(kkey).child("totime").setValue(ts);
                                    database.child("users").child(toUser.uid).child("threads").child(kkey).child("read").setValue("1");
                                }
                                @Override
                                public void onFailure(JSONObject response) {
                                    Log.e("OneSignalExample", "postNotification Failure: " + response);
                                }
                            });
                    Toast.makeText(this, "The disputed comment has been withdrawn.", Toast.LENGTH_SHORT).show();
                    getThreadsHistory();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private String getMessageKey( String other){
        String mKey = "";
        UserModel one = MyApp.getInstance().myProfile;
        String my_phone = one.phoneno;
        String other_num = other;
        int nCom = other_num.compareTo(my_phone);
        if(nCom > 0){
            mKey = my_phone + ":" + other_num;
        } else{
            mKey = other_num + ":" + my_phone;
        }
        return mKey;
    }

    private void getThreadsHistory(){
        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel("Waiting...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();

        String uid = MyApp.getInstance().myProfile.uid;
        database.child("users").child(uid).child("threads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hud.dismiss();
                threadlist = new ArrayList<>();
                for(DataSnapshot sh : dataSnapshot.getChildren()){
                    String key = sh.getKey();
                    ThreadModel one = new ThreadModel();
                    one.skey = key;
                    one.sid = (String)sh.child("toid").getValue();
                    one.sname = (String)sh.child("toname").getValue();
                    one.sphone = (String)sh.child("tophone").getValue();
                    one.msg = (String)sh.child("msg").getValue();
                    one.stime = (String)sh.child("totime").getValue();

                    threadlist.add(one);
                }
                if(threadlist.size() > 0){
                    recyclerView.setVisibility(View.VISIBLE);
                    tv_thread_nomail.setVisibility(View.GONE);
                } else{
                    recyclerView.setVisibility(View.GONE);
                    tv_thread_nomail.setVisibility(View.VISIBLE);
                }
                threadListAdapter.setDataList(threadlist);
                threadListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hud.dismiss();
            }
        });
        /*threadlist = mHelper.getAllThreads();
        if(threadlist.size() > 0){
            recyclerView.setVisibility(View.VISIBLE);
            tv_thread_nomail.setVisibility(View.GONE);
        } else{
            recyclerView.setVisibility(View.GONE);
            tv_thread_nomail.setVisibility(View.VISIBLE);
        }
        threadListAdapter.setDataList(threadlist);
        threadListAdapter.notifyDataSetChanged();*/
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
                Intent intent = new Intent(ThreadActivity.this, FeedActivity.class);
                startActivity(intent);
                finish();
            }
        });

        iv_home_message = (ImageView) findViewById(R.id.iv_home_message);
        iv_home_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();

            }
        });

        iv_home_search = (ImageView) findViewById(R.id.iv_home_search);
        iv_home_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ThreadActivity.this, HomeActivity.class);
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
                Intent intent = new Intent(ThreadActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        tv_menu_privacy = (TextView) findViewById(R.id.tv_menu_privacy);
        tv_menu_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(ThreadActivity.this, OtherActivity.class);
                intent.putExtra("title", "PRIVACY");
                startActivity(intent);
            }
        });

        tv_menu_terms = (TextView) findViewById(R.id.tv_menu_terms);
        tv_menu_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(ThreadActivity.this, OtherActivity.class);
                intent.putExtra("title", "TERMS & CONDITIONS");
                startActivity(intent);
            }
        });

        tv_menu_faq = (TextView) findViewById(R.id.tv_menu_faq);
        tv_menu_faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(ThreadActivity.this, OtherActivity.class);
                intent.putExtra("title", "FAQS");
                startActivity(intent);
            }
        });

        tv_menu_account = (TextView) findViewById(R.id.tv_menu_account);
        tv_menu_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(ThreadActivity.this, AccountActivity.class);
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
