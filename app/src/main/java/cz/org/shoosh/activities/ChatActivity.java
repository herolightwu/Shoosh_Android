package cz.org.shoosh.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.org.shoosh.MyApp;
import cz.org.shoosh.R;
import cz.org.shoosh.adapters.ChatListAdapter;
import cz.org.shoosh.adapters.FeedListAdapter;
import cz.org.shoosh.models.MessageModel;
import cz.org.shoosh.models.UserModel;
import cz.org.shoosh.utils.Constants;

public class ChatActivity extends AppCompatActivity {

    private final String Acivity_Tag = "Chat";

    String roomID;
    ImageView iv_home_back;
    RecyclerView recyclerView;
    List<MessageModel> message_list;
    ChatListAdapter chatListAdapter;

    LinearLayout ll_menu;
    ImageView iv_home_menu, iv_home_feed, iv_home_message, iv_home_search;
    TextView tv_menu_about, tv_menu_privacy, tv_menu_terms, tv_menu_faq, tv_menu_account, tv_menu_invite;

    DatabaseReference database;
    EditText et_chat_message;
    ImageView iv_chat_send, iv_chat_delete;
    TextView tv_chat_more;
    Boolean bshowhistory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomID = getIntent().getStringExtra("room_key");
        setContentView(R.layout.activity_chat);
        database = FirebaseDatabase.getInstance().getReference();

        initLayout();
        initMenus();
        getChatHistory();
    }

    @Override
    public void onResume(){
        super.onResume();
        MyApp.getInstance().topTitle = Acivity_Tag;
        getOtherInfo();
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

        message_list = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        chatListAdapter = new ChatListAdapter(message_list, getApplicationContext());
        recyclerView.setAdapter(chatListAdapter);

        chatListAdapter.setOnItemClickListener(new ChatListAdapter.OnItemClickListener() {
            @Override
            public int onItemClick(int position) {
                return position;
            }
        });

        et_chat_message = (EditText) findViewById(R.id.et_chat_message);
        iv_chat_send = (ImageView) findViewById(R.id.iv_chat_send);
        iv_chat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
                if(et_chat_message.getText().toString().trim().length() >0){
                    Long tsLong = System.currentTimeMillis()/1000;
                    String ts = tsLong.toString();
                    String uname = MyApp.getInstance().myProfile.uname;
                    String uid = MyApp.getInstance().myProfile.uid;
                    String mkey = database.child("messages").child(roomID).child("chat").push().getKey();
                    database.child("messages").child(roomID).child("chat").child(mkey).child("msg").setValue(et_chat_message.getText().toString());
                    database.child("messages").child(roomID).child("chat").child(mkey).child("stime").setValue(ts);
                    database.child("messages").child(roomID).child("chat").child(mkey).child("sid").setValue(uid);
                    database.child("messages").child(roomID).child("chat").child(mkey).child("sname").setValue(uname);
                    getChatHistory();
                    sendOneSignalNotification();

                    et_chat_message.setText("");
                }
            }
        });

        iv_chat_delete = (ImageView) findViewById(R.id.iv_chat_delete);
        iv_chat_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupDialog();
            }
        });

        tv_chat_more = (TextView) findViewById(R.id.tv_chat_more);
        tv_chat_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bshowhistory = true;
                getChatHistory();
            }
        });

        et_chat_message.clearFocus();
        recyclerView.setFocusable(true);
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void showPopupDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.popup_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);

        final TextView tv_content = (TextView) dialog.findViewById(R.id.tv_popup_content);
        tv_content.setText("Are you sure you want to delete these messages?");

        TextView txt_OK = (TextView) dialog.findViewById(R.id.txt_OK);
        txt_OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.child("messages").child(roomID).child("chat").setValue(null);
                getChatHistory();
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

    private void getOtherInfo(){
        MyApp.getInstance().otherUser = new UserModel();
        String[] separated = roomID.split(":");
        String other_no = separated[0];
        String myno = MyApp.getInstance().myProfile.phoneno;
        if(other_no.equals(myno)){
            other_no = separated[1];
        }
        for(UserModel one : MyApp.getInstance().allusers){
            String one_num = one.phoneno;
            if(other_no.equals(one_num)){
                UserModel other = new UserModel();
                other.uid = one.uid;
                other.uname = one.uname;
                other.phoneno = one.phoneno;
                other.devtype = one.devtype;
                other.token = one.token;
                MyApp.getInstance().otherUser = other;
                break;
            }
        }
    }

    private void sendOneSignalNotification(){
        final UserModel one = MyApp.getInstance().otherUser;

        String send_txt = et_chat_message.getText().toString();
        if(send_txt.length() > 30){
            send_txt = et_chat_message.getText().toString().substring(0, 30) + "...";
        }

        final String stxt = send_txt;
        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        boolean isSubscribed = status.getSubscriptionStatus().getSubscribed();
        if(isSubscribed){

            String body = MyApp.getInstance().myProfile.uname + " has sent you a message.";
            try {
                OneSignal.postNotification(new JSONObject("{'include_player_ids': ['" + one.token + "'], " +
                                "'contents': {'en':'" + body + "'}, " +
                                "'ios_badgeType': 'SetTo', " +
                                "'ios_badgeCount': 1, " +
                                "'data': {'type': 'thread', 'roomID': '" + roomID + "' , 'send_id': '"+ MyApp.getInstance().myProfile.uid +"', 'send_name': '" + MyApp.getInstance().myProfile.uname + "' , 'send_phone': '"+ MyApp.getInstance().myProfile.phoneno +"', 'msg': '" + send_txt +"' }}"),
                        new OneSignal.PostNotificationResponseHandler() {
                            @Override
                            public void onSuccess(JSONObject response) {

                                database.child("users").child(one.uid).child("threads").orderByChild("toid").equalTo(MyApp.getInstance().myProfile.uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Long tsLong = System.currentTimeMillis()/1000;
                                        String ts = tsLong.toString();
                                        if(dataSnapshot.hasChildren()){
                                            for(DataSnapshot sh : dataSnapshot.getChildren()){
                                                String ikey = sh.getKey();
                                                database.child("users").child(one.uid).child("threads").child(ikey).child("msg").setValue(stxt);
                                                database.child("users").child(one.uid).child("threads").child(ikey).child("totime").setValue(ts);
                                                database.child("users").child(one.uid).child("threads").child(ikey).child("read").setValue(null);
                                            }
                                        } else{
                                            String kkey = database.child("users").child(one.uid).child("threads").push().getKey();
                                            database.child("users").child(one.uid).child("threads").child(kkey).child("toid").setValue(MyApp.getInstance().myProfile.uid);
                                            database.child("users").child(one.uid).child("threads").child(kkey).child("toname").setValue(MyApp.getInstance().myProfile.uname);
                                            database.child("users").child(one.uid).child("threads").child(kkey).child("tophone").setValue(MyApp.getInstance().myProfile.phoneno);
                                            database.child("users").child(one.uid).child("threads").child(kkey).child("msg").setValue(stxt);
                                            database.child("users").child(one.uid).child("threads").child(kkey).child("totime").setValue(ts);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

                            @Override
                            public void onFailure(JSONObject response) {
                                Log.e("OneSignalExample", "postNotification Failure: " + response);
                            }
                        });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /*private void sendThreadNotification(){
        UserModel one = MyApp.getInstance().otherUser;
        JSONObject body = new JSONObject();

        String send_txt = et_chat_message.getText().toString();
        if(send_txt.length() > 30){
            send_txt = et_chat_message.getText().toString().substring(0, 30) + "...";
        }
        try{
            body.put("title", "Shoosh");
            body.put("body", MyApp.getInstance().myProfile.uname + " has sent you a message.");
            body.put("type", "thread");
            body.put("roomID", roomID);
            body.put("send_id", MyApp.getInstance().myProfile.uid);
            body.put("send_name", MyApp.getInstance().myProfile.uname);
            body.put("send_phone", MyApp.getInstance().myProfile.phoneno);
            body.put("msg", send_txt);

            //JSONObject noti = new JSONObject();
            //noti.put("title", "Shoosh");
            //noti.put("body", MyApp.getInstance().myProfile.uname + " has sent you a message.");
            JSONObject data  = new JSONObject();

            data.put("to", one.token);
            data.put("data", body);
            //data.put("notification", noti);
            //data.put("content-available", true);
            if(one.token != null){
                AndroidNetworking.post("https://fcm.googleapis.com/fcm/send")
                        .addHeaders("Content-Type", "application/json")
                        .addHeaders("Authorization", "key=" + getString(R.string.FCM_server_key))
                        .addJSONObjectBody(data)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {

                            }

                            @Override
                            public void onError(ANError anError) {
                                Toast.makeText(ChatActivity.this, anError.getErrorBody(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } catch (JSONException ex){

        }
    }*/

    private void getChatHistory(){

        database.child("messages").child(roomID).child("chat").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                message_list = new ArrayList<>();
                long criteriaTime = 0;
                List<MessageModel> templist = new ArrayList<>();
                for(DataSnapshot msg_shot : dataSnapshot.getChildren()){
                    MessageModel one = new MessageModel();
                    one.sname = (String)msg_shot.child("sname").getValue();
                    one.sid = (String)msg_shot.child("sid").getValue();
                    one.msg = (String)msg_shot.child("msg").getValue();
                    one.stime = (String)msg_shot.child("stime").getValue();
                    one.key = msg_shot.getKey();
                    long chattime  = Long.valueOf(one.stime);
                    if(criteriaTime < chattime) {
                        criteriaTime = chattime;
                    }
                    templist.add(one);
                }
                for(MessageModel each_one : templist){
                    long ch_time = Long.valueOf(each_one.stime);
                    if(bshowhistory || ((criteriaTime - ch_time) < 1200)) //last 20min
                    {
                        message_list.add(each_one);
                    } else{
                        bshowhistory = false;
                    }
                }

                if(bshowhistory){
                    tv_chat_more.setVisibility(View.GONE);
                } else{
                    tv_chat_more.setVisibility(View.VISIBLE);
                }
                chatListAdapter.setDataList(message_list);
                chatListAdapter.notifyDataSetChanged();
                if(message_list.size() > 4){
                    recyclerView.scrollToPosition(message_list.size() - 1);
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
                Intent intent = new Intent(ChatActivity.this, FeedActivity.class);
                startActivity(intent);
            }
        });

        iv_home_message = (ImageView) findViewById(R.id.iv_home_message);
        iv_home_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                finish();
            }
        });

        iv_home_search = (ImageView) findViewById(R.id.iv_home_search);
        iv_home_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, HomeActivity.class);
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
                Intent intent = new Intent(ChatActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        tv_menu_privacy = (TextView) findViewById(R.id.tv_menu_privacy);
        tv_menu_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(ChatActivity.this, OtherActivity.class);
                intent.putExtra("title", "PRIVACY");
                startActivity(intent);
            }
        });

        tv_menu_terms = (TextView) findViewById(R.id.tv_menu_terms);
        tv_menu_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(ChatActivity.this, OtherActivity.class);
                intent.putExtra("title", "TERMS & CONDITIONS");
                startActivity(intent);
            }
        });

        tv_menu_faq = (TextView) findViewById(R.id.tv_menu_faq);
        tv_menu_faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(ChatActivity.this, OtherActivity.class);
                intent.putExtra("title", "FAQS");
                startActivity(intent);
            }
        });

        tv_menu_account = (TextView) findViewById(R.id.tv_menu_account);
        tv_menu_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(ChatActivity.this, AccountActivity.class);
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
