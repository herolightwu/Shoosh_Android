package cz.org.shoosh;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import cz.org.shoosh.activities.HomeActivity;
import cz.org.shoosh.activities.Register_Activity;
import cz.org.shoosh.models.UserModel;
import cz.org.shoosh.utils.SaveSharedPrefrence;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST= 123;

    DatabaseReference database;
    private SaveSharedPrefrence sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(getApplicationContext());

        sharedPreferences = new SaveSharedPrefrence();
        database = FirebaseDatabase.getInstance().getReference();

        String bValue = sharedPreferences.getKeyContact(getApplicationContext());
        if(bValue.equals("1")){
            MyApp.getInstance().bContact = true;
        } else if(bValue.equals("2")){
            MyApp.getInstance().bContact = false; //initialize
            sharedPreferences.saveKeyContact(getApplicationContext(), "0");
        }
        bValue = sharedPreferences.getKeyNotification(getApplicationContext());
        if(bValue.equals("1")){
            MyApp.getInstance().bNoti = true;
        } else if(bValue.equals("2")){
            MyApp.getInstance().bNoti = true; //initialize
            sharedPreferences.saveKeyNotification(getApplicationContext(), "1");
        } else{
            MyApp.getInstance().bNoti = false;
        }

        //verifyPhoneNumber();
        checkPermission(this);
        getUserInfo();
    }

    private void getUserInfo(){
        final String userID = sharedPreferences.getKeyUserID(getApplicationContext());
        //final String userID = "-LM7sB6ciqHMN6k0XRio";//debug
        //final String userID = "-LM8L04BWsHpq_HX-7wa";//debug
        //final String userID = "-LMI0il5EssrfzPFNmEw";//debug
        if(!userID.equals("0")){
            database.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChildren()){
                        DataSnapshot user_snap = dataSnapshot.child("userinfo");
                        UserModel one = new UserModel();
                        one.uid = userID;
                        one.uname = (String)user_snap.child("uname").getValue();
                        one.phoneno = (String)user_snap.child("phoneno").getValue();
                        one.token = (String)user_snap.child("token").getValue();
                        one.devtype = (String)user_snap.child("devtype").getValue();
                        one.bNoti = true;
                        one.bHide = false;
                        /*if(user_snap.hasChild("noti_set")){
                            String sNoti = (String) user_snap.child("noti_set").getValue();
                            if(sNoti.equals("0")){
                                one.bNoti = false;
                            }
                        }*/
                        if(user_snap.hasChild("hideme")){
                            String sNoti = (String) user_snap.child("hideme").getValue();
                            if(sNoti.equals("1")){
                                one.bHide = true;
                            }
                        }
                        MyApp.getInstance().myProfile = one;

                        Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(i);
                        finish();

                    } else{
                        sharedPreferences.saveKeyUserID(getApplicationContext(), "0");
                        Intent i = new Intent(getApplicationContext(), Register_Activity.class);
                        startActivity(i);
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else{
            Intent i = new Intent(getApplicationContext(), Register_Activity.class);
            startActivity(i);
            finish();
        }
    }

    public static boolean checkPermission(final Context context)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED) {

                try {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //}
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    //code for deny
                }
                break;
        }
    }
}
