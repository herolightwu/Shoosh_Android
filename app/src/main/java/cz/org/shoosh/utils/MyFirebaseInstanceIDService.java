package cz.org.shoosh.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import cz.org.shoosh.MyApp;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private String TAG = "Shoosh";

    SaveSharedPrefrence sharedPreferences;
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(final String refToken){
        final DatabaseReference database;
        sharedPreferences = new SaveSharedPrefrence();
        database = FirebaseDatabase.getInstance().getReference();
        final String uid = sharedPreferences.getKeyUserID(getApplicationContext());//MyApp.getInstance().myProfile.uid;
        database.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    database.child("users").child(uid).child("userinfo").child("token").setValue(refToken);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        MyApp.getInstance().myProfile.token = refToken;
    }
}
