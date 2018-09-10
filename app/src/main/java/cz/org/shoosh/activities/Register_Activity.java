package cz.org.shoosh.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
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
import java.util.concurrent.TimeUnit;

import cz.org.shoosh.MyApp;
import cz.org.shoosh.R;
import cz.org.shoosh.models.UserModel;
import cz.org.shoosh.utils.Constants;
import cz.org.shoosh.utils.SaveSharedPrefrence;

public class Register_Activity extends AppCompatActivity {

    TextView tv_title, tv_again, tv_terms;
    Button btn_next;
    EditText et_reg_value;
    private int act_type = Constants.ACT_TYPE_ENTER_NUMBER;

    TextView tv_reg_country_code;
    ImageView iv_reg_country_flag;
    LinearLayout ll_reg_country;
    CountryPicker picker;
    String selectCode = "AU";

    DatabaseReference database;
    private SaveSharedPrefrence sharedPreferences;
    UserModel user_temp;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_register);
        database = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = new SaveSharedPrefrence();

        user_temp = new UserModel();
        initLayout();
        setFunctions();
    }

    @Override
    public void onResume(){
        super.onResume();
        getAllUsers();
        refreshLayout();
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

    private void initLayout(){
        btn_next = (Button) findViewById(R.id.btn_next);
        et_reg_value = (EditText) findViewById(R.id.et_reg_value);
        tv_title = (TextView) findViewById(R.id.tv_reg_title);
        tv_again = (TextView) findViewById(R.id.tv_reg_again);
        tv_terms = (TextView) findViewById(R.id.tv_reg_terms);
        ll_reg_country = (LinearLayout) findViewById(R.id.ll_reg_country);
        tv_reg_country_code = (TextView) findViewById(R.id.tv_reg_country_code);
        iv_reg_country_flag = (ImageView) findViewById(R.id.iv_reg_country_flag);

        et_reg_value.setText("");
        et_reg_value.setFocusable(true);
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
    }

    private void refreshLayout(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tv_terms.setText(Html.fromHtml("<u>By proceeding you understand the <font color=\"gray\">Terms and Conditions</font></u>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            tv_terms.setText(Html.fromHtml("<u>By proceeding you understand the <font color=\"gray\">Terms and Conditions</font></u>"));
        }

        if(act_type == Constants.ACT_TYPE_ENTER_NUMBER){
            tv_title.setText(R.string.reg_tile_number);
            tv_again.setVisibility(View.INVISIBLE);
            tv_terms.setVisibility(View.VISIBLE);
            //tv_terms.setText(R.string.reg_terms_string);
            //tv_terms.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
            ll_reg_country.setVisibility(View.VISIBLE);
            tv_reg_country_code.setVisibility(View.VISIBLE);
            btn_next.setText("Next");
            int maxLength = 12;
            InputFilter[] fArray = new InputFilter[1];
            fArray[0] = new InputFilter.LengthFilter(maxLength);
            et_reg_value.setFilters(fArray);
            et_reg_value.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        } else if(act_type == Constants.ACT_TYPE_CREATE_USERNAME){
            tv_title.setText(R.string.reg_tile_username);
            tv_again.setVisibility(View.VISIBLE);
            tv_terms.setVisibility(View.INVISIBLE);
            tv_again.setText(R.string.reg_protect_string);
            tv_again.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
            ll_reg_country.setVisibility(View.GONE);
            tv_reg_country_code.setVisibility(View.INVISIBLE);
            btn_next.setText("Next");
            int maxLength = 20;
            InputFilter[] fArray = new InputFilter[1];
            fArray[0] = new InputFilter.LengthFilter(maxLength);
            et_reg_value.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
            et_reg_value.setFilters(fArray);
        } else{
            tv_title.setText(R.string.reg_tile_code);
            tv_again.setVisibility(View.VISIBLE);
            tv_terms.setVisibility(View.INVISIBLE);
            tv_again.setText(R.string.reg_code_again_string);
            tv_again.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
            ll_reg_country.setVisibility(View.GONE);
            tv_reg_country_code.setVisibility(View.INVISIBLE);
            btn_next.setText("Sign up");
            int maxLength = 10;
            InputFilter[] fArray = new InputFilter[1];
            fArray[0] = new InputFilter.LengthFilter(maxLength);
            et_reg_value.setFilters(fArray);
            et_reg_value.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        }
        et_reg_value.setText("");
    }

    private void setFunctions(){
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(et_reg_value.getText().toString().length() > 0){
                    if(act_type == Constants.ACT_TYPE_ENTER_NUMBER){
                        String nphone = MyApp.parseContact(et_reg_value.getText().toString().trim(), selectCode);
                        if(nphone != null){
                            act_type = Constants.ACT_TYPE_CREATE_USERNAME;
                            user_temp.phoneno = tv_reg_country_code.getText().toString() + et_reg_value.getText().toString().trim();
                            refreshLayout();
                        } else{
                            Toast.makeText(Register_Activity.this, "Phone number is not valid.", Toast.LENGTH_SHORT).show();
                        }

                    } else if(act_type == Constants.ACT_TYPE_CREATE_USERNAME){
                        act_type = Constants.ACT_TYPE_ENTER_CODE;
                        user_temp.uname = et_reg_value.getText().toString();
                        verifyWithPhone();
                        refreshLayout();

                        showPopupDialog();
                    } else{
                        user_temp.devtype = "Android";
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, et_reg_value.getText().toString().trim());
                        signInWithPhoneAuthCredential(credential);

                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please type value", Toast.LENGTH_SHORT).show();
                }

            }
        });

        tv_terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Register_Activity.this, OtherActivity.class);
                intent.putExtra("title", "TERMS & CONDITIONS");
                intent.putExtra("type", 2);
                startActivity(intent);
            }
        });

        tv_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(act_type == Constants.ACT_TYPE_ENTER_CODE){
                    resendVerificationCode(mResendToken);
                    showPopupDialog();
                }
            }
        });

        ll_reg_country.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
            }
        });
    }

    private void verifyWithPhone(){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                user_temp.phoneno,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        //Log.d("History", "onVerificationCompleted:" + phoneAuthCredential);

                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        //Log.w(TAG, "onVerificationFailed", e);

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            // ...
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // The SMS quota for the project has been exceeded
                            // ...
                        }
                        Toast.makeText(Register_Activity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        // Show a message and update the UI
                        // ...
                    }

                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken token) {
                        // The SMS verification code has been sent to the provided phone number, we
                        // now need to ask the user to enter the code and then construct a credential
                        // by combining the code with a verification ID.
                        //Log.d(TAG, "onCodeSent:" + verificationId);

                        // Save verification ID and resending token so we can use them later
                        mVerificationId = verificationId;
                        mResendToken = token;
                        //Toast.makeText(Register_Activity.this, "Please wait. your authentication code is being sent via text message.", Toast.LENGTH_LONG).show();
                    }
                });        // OnVerificationStateChangedCallbacks
    }

    private void resendVerificationCode(PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                user_temp.phoneno,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(Register_Activity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken token) {
                        // The SMS verification code has been sent to the provided phone number, we
                        // now need to ask the user to enter the code and then construct a credential
                        // by combining the code with a verification ID.
                        //Log.d(TAG, "onCodeSent:" + verificationId);

                        // Save verification ID and resending token so we can use them later
                        mVerificationId = verificationId;
                        mResendToken = token;
                        //Toast.makeText(Register_Activity.this, "Please wait. your authentication code is being sent via text message.", Toast.LENGTH_LONG).show();

                    }
                },         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel("Waiting...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            //FirebaseUser user = task.getResult().getUser();
                            hud.dismiss();
                            signupToFirebase();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(Register_Activity.this, "The verification code entered was invalid. Please try to enter code again.", Toast.LENGTH_SHORT).show();
                            }
                            hud.dismiss();
                        }

                    }
                });
    }

    private void signupToFirebase(){

        final KProgressHUD hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setWindowColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setLabel("Registering...")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        List<UserModel> allusers = MyApp.getInstance().allusers;
        for(UserModel one : allusers){
            if(one.phoneno.equals(user_temp.phoneno)){
                hud.dismiss();
                registerUser(one.uid);
                finish();
                return;
            }
        }

        String userID = database.child("users").push().getKey();
        hud.dismiss();
        registerUser(userID);
        finish();
    }

    private void registerUser(String uid){
        database.child("users").child(uid).child("userinfo").child("uname").setValue(user_temp.uname);
        database.child("users").child(uid).child("userinfo").child("phoneno").setValue(user_temp.phoneno);

        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        String oneSignalId = status.getSubscriptionStatus().getUserId();
        database.child("users").child(uid).child("userinfo").child("token").setValue(oneSignalId);
        database.child("users").child(uid).child("userinfo").child("devtype").setValue("Android");

        MyApp.getInstance().myProfile.uid = uid;
        MyApp.getInstance().myProfile.phoneno = user_temp.phoneno;
        MyApp.getInstance().myProfile.uname = user_temp.uname;
        MyApp.getInstance().myProfile.devtype = "Android";
        MyApp.getInstance().myProfile.token = oneSignalId;

        sharedPreferences.saveKeyUserID(getApplicationContext(), uid);
        sharedPreferences.saveKeyPhone(getApplicationContext(), user_temp.phoneno);

        /*Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        database.child("users").child(userID).child("commentinfo").child("count").setValue("0");
        database.child("users").child(userID).child("commentinfo").child("stime").setValue(ts);*/

        Intent intent = new Intent( Register_Activity.this, HomeActivity.class);
        startActivity(intent);
    }

    public void showPopupDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.popup_rate_dlg);
        View dview = dialog.getWindow().getDecorView();
        dview.setBackgroundResource(android.R.color.transparent);

        final TextView tv_content = (TextView) dialog.findViewById(R.id.tv_popup_content);
        String stxt = "Please wait. Your authentication code is being sent via text message.";
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
}
