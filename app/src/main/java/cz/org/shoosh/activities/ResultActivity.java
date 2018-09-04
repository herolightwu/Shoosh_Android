package cz.org.shoosh.activities;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.org.shoosh.MyApp;
import cz.org.shoosh.R;
import cz.org.shoosh.adapters.ResultListAdapter;
import cz.org.shoosh.models.CommentModel;
import cz.org.shoosh.models.UserModel;
import cz.org.shoosh.utils.Constants;

public class ResultActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler{

    ImageView iv_home_back;
    RelativeLayout rl_result_none;
    LinearLayout ll_result;
    RecyclerView recyclerView;
    List<CommentModel> resultlist;
    ResultListAdapter resultListAdapter;
    EditText et_result_comment;
    TextView tv_result_reviews;
    ImageView iv_result_new_comment, iv_result_send;

    String search_number;

    LinearLayout ll_menu;
    ImageView iv_home_menu, iv_home_feed, iv_home_message, iv_home_search;
    TextView tv_menu_about, tv_menu_privacy, tv_menu_terms, tv_menu_faq, tv_menu_account, tv_menu_invite;

    DatabaseReference database;

    private int mEditType = Constants.COMMENT_NEW_SEND;
    private int sel_pos = -1;

    private BillingProcessor bp;
    private boolean readyToPurchase = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_result);
        search_number = getIntent().getStringExtra("search_number");
        initLayout();
        initMenus();
        initIAP();
        searchUserInfo();
    }

    public String parseLocalContact(String contact, String countrycode) {
        Phonenumber.PhoneNumber phoneNumber = null;
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        String finalNumber = null;
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
                    PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
        }

        return finalNumber;
    }

    private void initIAP(){
        bp = new BillingProcessor(this, Constants.LICENSE_KEY, this);//bp = new BillingProcessor(this, LICENSE_KEY, MERCHANT_ID, this);

    }

    private void initLayout(){
        et_result_comment = (EditText) findViewById(R.id.et_result_comment);
        tv_result_reviews = (TextView) findViewById(R.id.tv_result_reviews);
        iv_result_send = (ImageView) findViewById(R.id.iv_result_send);
        iv_result_new_comment = (ImageView) findViewById(R.id.iv_result_new_comment);
        iv_home_back = (ImageView) findViewById(R.id.iv_home_back);
        iv_home_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        iv_result_new_comment.setVisibility(View.GONE);

        iv_result_new_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditType = Constants.COMMENT_NEW_SEND;
                rl_result_none.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                et_result_comment.setFocusable(true);
                et_result_comment.setText("");
            }
        });

        iv_result_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uid = MyApp.getInstance().myProfile.uid;
                if(mEditType == Constants.COMMENT_NEW_SEND){
                    if(et_result_comment.getText().toString().length() > 0){
                        Long tsLong = System.currentTimeMillis()/1000;
                        String ts = tsLong.toString();
                        String uname = MyApp.getInstance().myProfile.uname;
                        String com_key = database.child("comments").child(search_number).push().getKey();
                        database.child("comments").child(search_number).child(com_key).child("uid").setValue(uid);
                        database.child("comments").child(search_number).child(com_key).child("uname").setValue(uname);
                        database.child("comments").child(search_number).child(com_key).child("content").setValue(et_result_comment.getText().toString());
                        database.child("comments").child(search_number).child(com_key).child("timestamp").setValue(ts);
                        database.child("comments").child(search_number).child(com_key).child("uphone").setValue(MyApp.getInstance().myProfile.phoneno);
                        searchComments();
                        sendCommentNotification();
                    } else{
                        Toast.makeText(ResultActivity.this, "Please type comment.", Toast.LENGTH_SHORT).show();
                    }
                } else if(mEditType == Constants.COMMENT_EDIT_SEND){
                    if(et_result_comment.getText().toString().length() > 0) {
                        CommentModel one = resultlist.get(sel_pos);
                        Long tsLong = System.currentTimeMillis()/1000;
                        String ts = tsLong.toString();
                        String uname = MyApp.getInstance().myProfile.uname;
                        String com_key = one.mkey;
                        database.child("comments").child(search_number).child(com_key).child("uid").setValue(uid);
                        database.child("comments").child(search_number).child(com_key).child("uname").setValue(uname);
                        database.child("comments").child(search_number).child(com_key).child("content").setValue(et_result_comment.getText().toString());
                        database.child("comments").child(search_number).child(com_key).child("timestamp").setValue(ts);
                        database.child("comments").child(search_number).child(com_key).child("uphone").setValue(MyApp.getInstance().myProfile.phoneno);
                        database.child("comments").child(search_number).child(com_key).child("read").setValue(null);
                        searchComments();
                        sendCommentNotification();
                    } else{
                        Toast.makeText(ResultActivity.this, "Please type comment.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        rl_result_none = (RelativeLayout) findViewById(R.id.rl_result_none);
        ll_result = (LinearLayout) findViewById(R.id.ll_result);
        tv_result_reviews.setText("0 Result for " + search_number);

        resultlist = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        resultListAdapter = new ResultListAdapter(resultlist, getApplicationContext(), search_number);
        recyclerView.setAdapter(resultListAdapter);

        resultListAdapter.setOnItemClickListener(new ResultListAdapter.OnItemClickListener() {
            @Override
            public int onItemClick(int position) {
                if(!MyApp.getInstance().bPaid){
                    showPopupDialog();
                }
                return position;
            }
        });

        resultListAdapter.setOnItemDeleteListener(new ResultListAdapter.OnItemDeleteListener() {
            @Override
            public int onItemDelete(int position) {
                CommentModel one = resultlist.get(position);
                if(one.uid.equals(MyApp.getInstance().myProfile.uid)){
                    database.child("comments").child(search_number).child(one.mkey).setValue(null);
                    searchComments();
                } else{
                    Toast.makeText(ResultActivity.this, "You can not delete this comment.", Toast.LENGTH_SHORT).show();
                }
                return position;
            }
        });

        resultListAdapter.setOnItemEditListener(new ResultListAdapter.OnItemEditListener() {
            @Override
            public int onItemEdit(int position) {
                sel_pos = position;
                CommentModel one = resultlist.get(position);
                mEditType = Constants.COMMENT_EDIT_SEND;
                iv_result_new_comment.setVisibility(View.GONE);
                rl_result_none.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                et_result_comment.setFocusable(true);
                et_result_comment.setText(one.content);
                return position;
            }
        });

        resultListAdapter.setmOnItemContactListener(new ResultListAdapter.OnItemContactListener() {
            @Override
            public int onItemContact(int position) {
                if(!MyApp.getInstance().bPaid){
                    showPopupDialog();
                } else{
                    //UserModel other = MyApp.getInstance().otherUser;
                    String uid = MyApp.getInstance().myProfile.uid;
                    CommentModel one = resultlist.get(position);
                    if(one.uid != null && !uid.equals(one.uid)){
                        moveChatRoomById(one.uid);
                    }
                }

                return position;
            }
        });

        resultListAdapter.setOnItemReportListener(new ResultListAdapter.OnItemReportListener() {
            @Override
            public int onItemReport(int position) {
                showReportDialog(position);
                return position;
            }
        });

        manageBlinkEffect();
    }

    private void manageBlinkEffect(){
        Animation animation = new AlphaAnimation(1, 0.3f); //to change visibility from visible to invisible
        animation.setDuration(1500); //1 second duration for each animation cycle
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE); //repeating indefinitely
        animation.setRepeatMode(Animation.REVERSE); //animation will start from end point once ended.
        iv_result_new_comment.startAnimation(animation); //to start animation
    }

    private void processReportComment(int pos){
        String uid = MyApp.getInstance().myProfile.uid;
        CommentModel one = resultlist.get(pos);
        String send_txt = one.content;
        if(send_txt.length() > 80){
            send_txt = one.content.substring(0, 79) + "...";
        }
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        database.child("comments").child(search_number).child(one.mkey).child("report").setValue(ts);
        List<UserModel> allusers = MyApp.getInstance().allusers;
        for(UserModel one_user : allusers){
            String user_num = one_user.uid;
            if(user_num.equals(one.uid)){
                sendReportMessage(one.mkey, one_user, send_txt);
				break;
            }
        }
        searchComments();
    }

    private void sendReportMessage(final String ckey, final UserModel toUser, final String sendtxt){
        String body = MyApp.getInstance().myProfile.uname + " has disputed your comment.";
        try {
            OneSignal.postNotification(new JSONObject("{'include_player_ids': ['" + toUser.token + "'], " +
                            "'contents': {'en':'" + body + "'}, " +
                            "'ios_badgeType': 'SetTo', " +
                            "'ios_badgeCount': 1, " +
                            "'data': {'type': 'thread', 'roomID': '" + "report" + "' , 'send_id': '"+ MyApp.getInstance().myProfile.uid +"', 'send_name': '" + MyApp.getInstance().myProfile.uname + "' , 'send_phone': '"+ search_number +"', 'msg': '" + ckey +"' }}"),
                    new OneSignal.PostNotificationResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            String msgtxt = ckey + ":::" + sendtxt;
                            Long tsLong = System.currentTimeMillis()/1000;
                            String ts = tsLong.toString();
                            String kkey = database.child("users").child(toUser.uid).child("threads").push().getKey();
                            database.child("users").child(toUser.uid).child("threads").child(kkey).child("toid").setValue(MyApp.getInstance().myProfile.uid);
                            database.child("users").child(toUser.uid).child("threads").child(kkey).child("toname").setValue(MyApp.getInstance().myProfile.uname);
                            database.child("users").child(toUser.uid).child("threads").child(kkey).child("tophone").setValue("Shoosh");
                            database.child("users").child(toUser.uid).child("threads").child(kkey).child("msg").setValue(msgtxt);
                            database.child("users").child(toUser.uid).child("threads").child(kkey).child("totime").setValue(ts);
                            database.child("users").child(toUser.uid).child("threads").child(kkey).child("read").setValue("1");

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

    private void showReportDialog(final int pos) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.popup_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);

        final TextView tv_content = (TextView) dialog.findViewById(R.id.tv_popup_content);
        tv_content.setText("The author of this comment has 48 hours to respond. After 48 hours, if the author has not responded, this comment will be deleted. Continue?");

        TextView txt_OK = (TextView) dialog.findViewById(R.id.txt_OK);
        txt_OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processReportComment(pos);
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

    private void sendCommentNotification(){
        //String ssnumber = search_number.replace("+", "");
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = manager.getNetworkCountryIso();
        String nation_num = parseLocalContact(search_number, countryCode);
        nation_num = nation_num.replace(" ", "");

        List<UserModel> allusers = MyApp.getInstance().allusers;
        for(UserModel one_user : allusers){
            String user_num = one_user.phoneno;
            if(!one_user.uid.equals(MyApp.getInstance().myProfile.uid) && !user_num.equals(search_number) && one_user.contactlist.length() > 3 && one_user.bNoti){
                String[] separated = one_user.contactlist.split(",");
                for(String one_num : separated){
                    String[] foo = one_num.split(":");
                    String a_pre = "";
                    if(foo.length == 1) continue;
                    if(foo[1].length() > 0 ) a_pre = foo[1].substring(0,1);
                    if(a_pre.equals("+") && foo[1].equals(search_number)){
                        if(foo[0].length() == 0){
                            sendOneSignalNotification(one_user.uid, one_user.token, "Unknown Name");
                        } else{
                            sendOneSignalNotification(one_user.uid, one_user.token, foo[0]);
                        }
                    } else if(foo[1].equals(nation_num)){
                        if(foo[0].length() == 0){
                            sendOneSignalNotification(one_user.uid, one_user.token, "Unknown Name");
                        } else{
                            sendOneSignalNotification(one_user.uid, one_user.token, foo[0]);
                        }
                    }
                }
            }
                //sendOneSignalNotification(one_user);
        }
    }
    private void sendOneSignalNotification(final String uID, final String sstoken, final String ssname){
        if(sstoken.length() == 0) return;
        final UserModel to_user = MyApp.getInstance().otherUser;
        String bodystr = ssname + " has just received a comment.";
        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        boolean isSubscribed = status.getSubscriptionStatus().getSubscribed();
        if(isSubscribed){
            try {
                OneSignal.postNotification(new JSONObject("{'include_player_ids': ['" + sstoken + "'], " +
                                "'contents': {'en':'" + bodystr + "'}, " +
                                "'ios_badgeType': 'SetTo', " +
                                "'ios_badgeCount': 1, " +
                                "'data': {'type': 'comment', 'touser': '" + to_user.uid + "' , 'toname': '"+ ssname +"', 'tophone': '" + search_number +"' }}"),
                        new OneSignal.PostNotificationResponseHandler() {
                            @Override
                            public void onSuccess(JSONObject response) {
                                Long tsLong = System.currentTimeMillis()/1000;
                                String ts = tsLong.toString();
                                String kkey = database.child("users").child(uID).child("feeds").push().getKey();
                                database.child("users").child(uID).child("feeds").child(kkey).child("toid").setValue(to_user.uid);
                                database.child("users").child(uID).child("feeds").child(kkey).child("toname").setValue(ssname);
                                database.child("users").child(uID).child("feeds").child(kkey).child("tophone").setValue(search_number);
                                database.child("users").child(uID).child("feeds").child(kkey).child("totime").setValue(ts);
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

    private void sendOneSignalNotification(UserModel one){
        if(one.token.length() == 0) return;
        UserModel to_user = MyApp.getInstance().otherUser;
        String toname = to_user.uname;
        if(toname == null) toname = search_number;
        String bodystr = toname + " has just received a comment.";
        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        boolean isSubscribed = status.getSubscriptionStatus().getSubscribed();
        if(isSubscribed){
            try{
                JSONArray ids = new JSONArray();
                ids.put(one.token);
                JSONObject body = new JSONObject();
                body.put("type", "comment");
                body.put("touser", to_user.uid);
                body.put("toname", toname);
                body.put("tophone", search_number);

                JSONObject content = new JSONObject();
                content.put("en", bodystr);
                JSONObject data  = new JSONObject();
                data.put("include_player_ids", ids);
                data.put("contents", content);
                data.put("ios_badgeType", "SetTo");
                data.put("ios_badgeCount", 1);
                data.put("data", body);

                OneSignal.postNotification(data, new OneSignal.PostNotificationResponseHandler() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        Log.i("OneSignal", "Post success:" + response);
                    }

                    @Override
                    public void onFailure(JSONObject response) {
                        Log.i("OneSignal", "Post failure:" + response);
                    }
                });
            } catch (JSONException e){
                e.printStackTrace();
            }
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
        tv_content.setText("You are required to upgrade.\nContinue?");

        TextView txt_OK = (TextView) dialog.findViewById(R.id.txt_OK);
        txt_OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buySubscribe();
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

    private void searchComments(){
        //MyApp.getInstance().bPaid = true;//debug
        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel("Waiting...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        database.child("comments").child(search_number).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    rl_result_none.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    resultlist = new ArrayList<>();
                    Boolean bself = false;
                    for(DataSnapshot comment_key : dataSnapshot.getChildren()){
                        CommentModel one = new CommentModel();
                        one.mkey = (String) comment_key.getKey();
                        one.uid = (String) comment_key.child("uid").getValue();
                        one.name = (String) comment_key.child("uname").getValue();
                        one.content = (String) comment_key.child("content").getValue();
                        one.feedtime = (String) comment_key.child("timestamp").getValue();
                        one.uphone = (String) comment_key.child("uphone").getValue();
                        if(one.uphone == null) continue;
                        if(one.uid.equals(MyApp.getInstance().myProfile.uid)){//one.uid.equals(MyApp.getInstance().myProfile.uid) &&
                            bself = true;
                        }

                        if(checkContactAvailable(one.uphone)){
                            one.bContact = true;
                        } else{
                            one.bContact = false;
                        }

                        if(MyApp.getInstance().bPaid){
                            one.bDisp = true;
                        } else{
                            if(checkViewedComments(one.mkey, one.uid)){
                                one.bDisp = true;
                            } else{
                                one.bDisp = false;
                            }
                        }

                        one.breport = false;
                        if(comment_key.hasChild("report")){
                            one.breport = true;
                            Long tsLong = System.currentTimeMillis()/1000;
                            String rp_str = (String) comment_key.child("report").getValue();
                            Long tsReport = Long.parseLong(rp_str);
                            if(tsReport != 0 && tsLong - tsReport > 48*3600){
                                database.child("comments").child(search_number).child(one.mkey).setValue(null);
                                continue;
                            }
                        }

                        resultlist.add(one);
                    }

                    if(bself || search_number.equals(MyApp.getInstance().myProfile.phoneno)){
                        iv_result_new_comment.setVisibility(View.GONE);
                    } else{
                        iv_result_new_comment.setVisibility(View.VISIBLE);
                    }

                    String disp_str = resultlist.size() + " Results for " + search_number;
                    tv_result_reviews.setText(disp_str);

                    resultListAdapter.setDataList(resultlist);
                    resultListAdapter.notifyDataSetChanged();

                } else{
                    mEditType = Constants.COMMENT_NEW_SEND;
                    iv_result_new_comment.setVisibility(View.GONE);
                    rl_result_none.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    tv_result_reviews.setText("0 Result for " + search_number);
                    et_result_comment.setFocusable(true);
                    et_result_comment.setText("");
                }
                hud.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                hud.dismiss();
                Toast.makeText(ResultActivity.this, "DB Connection failed. Please try to search again.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private boolean checkViewedComments(String cId, String uId){
        String uid = MyApp.getInstance().myProfile.uid;
        if(uId.equals(uid)) return true;
        for(String ss : MyApp.getInstance().viewedComments){
            if(ss.equals(cId)){
                return true;
            }
        }
        if(MyApp.getInstance().viewedComments.size() < 5){
            MyApp.getInstance().viewedComments.add(cId);
            String ins_key = database.child("users").child(uid).child("viewedcomments").push().getKey();
            database.child("users").child(uid).child("viewedcomments").child(ins_key).setValue(cId);
            return true;
        } else{
            return false;
        }
    }

    private boolean checkContactAvailable(String uphone){
        List<UserModel> allusers = MyApp.getInstance().allusers;
        for(UserModel one_user : allusers){
            String one_num = one_user.phoneno;
            if(one_num.equals(uphone)) {
                return !one_user.bHide;
            }
        }
        return false;
    }

    private void searchUserInfo(){
        if(search_number.equals(MyApp.getInstance().myProfile.phoneno)){
            MyApp.getInstance().otherUser = MyApp.getInstance().myProfile;
            return;
        }
        List<UserModel> allusers = MyApp.getInstance().allusers;
        MyApp.getInstance().otherUser = new UserModel();

        for(UserModel one_user : allusers){
            String one_num = one_user.phoneno;
            if(one_num.equals(search_number)){
                UserModel one = new UserModel();
                one.uid = one_user.uid;
                one.uname = one_user.uname;
                one.phoneno = one_user.phoneno;
                one.token = one_user.token;
                one.devtype = one_user.devtype;
                MyApp.getInstance().otherUser = one;
            }
        }
    }

    private void moveChatRoomById(String otherID){

        database.child("users").child(otherID).child("userinfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String phone2 = (String) dataSnapshot.child("phoneno").getValue();
                String mKey = "";
                UserModel one = MyApp.getInstance().myProfile;

                int nCom = phone2.compareTo(one.phoneno);
                if(nCom > 0){
                    mKey = one.phoneno + ":" + phone2;
                } else{
                    mKey = phone2 + ":" + one.phoneno;
                }

                Intent intent = new Intent(ResultActivity.this, ChatActivity.class);
                intent.putExtra("room_key", mKey);
                startActivity(intent);
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
                Intent intent = new Intent(ResultActivity.this, FeedActivity.class);
                startActivity(intent);
            }
        });

        iv_home_message = (ImageView) findViewById(R.id.iv_home_message);
        iv_home_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(ResultActivity.this, ThreadActivity.class);
                startActivity(intent);
            }
        });

        iv_home_search = (ImageView) findViewById(R.id.iv_home_search);
        iv_home_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
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
                Intent intent = new Intent(ResultActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        tv_menu_privacy = (TextView) findViewById(R.id.tv_menu_privacy);
        tv_menu_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(ResultActivity.this, OtherActivity.class);
                intent.putExtra("title", "PRIVACY");
                startActivity(intent);
            }
        });

        tv_menu_terms = (TextView) findViewById(R.id.tv_menu_terms);
        tv_menu_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(ResultActivity.this, OtherActivity.class);
                intent.putExtra("title", "TERMS & CONDITIONS");
                startActivity(intent);
            }
        });

        tv_menu_faq = (TextView) findViewById(R.id.tv_menu_faq);
        tv_menu_faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(ResultActivity.this, OtherActivity.class);
                intent.putExtra("title", "FAQS");
                startActivity(intent);
            }
        });

        tv_menu_account = (TextView) findViewById(R.id.tv_menu_account);
        tv_menu_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuSlideUp();
                Intent intent = new Intent(ResultActivity.this, AccountActivity.class);
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
        shareIntent.setType("text/*");
        startActivity(Intent.createChooser(shareIntent, "Invite A Friend"));
    }

    public void buySubscribe(){
        if (!readyToPurchase) {
            Toast.makeText(this,"Billing not initialized.", Toast.LENGTH_SHORT).show();
            return;
        }
        bp.subscribe(this, Constants.PRODUCT_ID);
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        if (bp.isSubscribed(Constants.PRODUCT_ID)) {
            MyApp.getInstance().bPaid = true;
            searchComments();
        }
    }
    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {

    }
    @Override
    public void onBillingInitialized() {
        readyToPurchase = true;
        updateSubscribe();
    }
    @Override
    public void onPurchaseHistoryRestored() {
        updateSubscribe();
    }

    private void updateSubscribe(){
        if (bp.isSubscribed(Constants.PRODUCT_ID)) {
            MyApp.getInstance().bPaid = true;
        }
        //MyApp.getInstance().bPaid = true;//debug
        searchComments();
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

    @Override
    public void onResume(){
        super.onResume();
        searchComments();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
