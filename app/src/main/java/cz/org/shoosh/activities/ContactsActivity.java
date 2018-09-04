package cz.org.shoosh.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cz.org.shoosh.MyApp;
import cz.org.shoosh.R;
import cz.org.shoosh.adapters.ContactsListAdapter;
import cz.org.shoosh.models.ContactModel;

public class ContactsActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST= 123;

    RecyclerView recyclerView;
    ImageView iv_home_back, iv_search_act;
    List<ContactModel> contactslist;
    ContactsListAdapter contactsListAdapter;
    EditText et_contact_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        initLayout();
    }

    private void initLayout(){
        iv_home_back = (ImageView) findViewById(R.id.iv_home_back);
        iv_home_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        iv_search_act = (ImageView) findViewById(R.id.iv_search_act);
        iv_search_act.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(et_contact_search.getVisibility() == View.GONE){
                    iv_search_act.setImageResource(R.drawable.ic_cancel);
                    et_contact_search.setText("");
                    et_contact_search.setVisibility(View.VISIBLE);
                } else{
                    iv_search_act.setImageResource(R.drawable.ic_search_ed);
                    et_contact_search.setVisibility(View.GONE);
                    contactsListAdapter.getFilter().filter("");
                }
            }
        });

        et_contact_search = (EditText) findViewById(R.id.et_contact_search);
        et_contact_search.setVisibility(View.GONE);

        contactslist = MyApp.getInstance().allContacts;
        //initList();
        recyclerView = (RecyclerView) findViewById(R.id.cont_recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        contactsListAdapter = new ContactsListAdapter(contactslist, getApplicationContext());
        recyclerView.setAdapter(contactsListAdapter);

        contactsListAdapter.setOnItemClickListener(new ContactsListAdapter.OnItemClickListener() {
            @Override
            public int onItemClick(String phoneStr) {
                String phoneno = phoneStr.replace(" ", "");
                String tempstr = phoneno.replace("-","");
                phoneno = tempstr.replace("(","");
                tempstr = phoneno.replace(")","");

                TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String countryCode = manager.getNetworkCountryIso();
                phoneno = MyApp.parseContact(tempstr, countryCode.toUpperCase());
                //phoneno = MyApp.parseContact("015940019735", "CN");
                if(phoneno != null){
                    Intent intent = new Intent(ContactsActivity.this, ResultActivity.class);
                    intent.putExtra("search_number", phoneno);
                    startActivity(intent);
                    finish();
                } else{
                    Toast.makeText(ContactsActivity.this, "Selected number is not valid.", Toast.LENGTH_SHORT).show();
                }

                return 0;
            }
        });

        //getContactList();

        et_contact_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                contactsListAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        et_contact_search.clearFocus();
    }



    private void getContactList() {
        contactslist = new ArrayList<>();
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
                            String phoneNo = pCur.getString(pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String photoStr = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_THUMBNAIL_URI));
                            if(phoneNo.length() > 0 && name.length() > 0){
                                if(checkNoExistList(phoneNo)){
                                    ContactModel one = new ContactModel();
                                    one.name = name;
                                    one.phone = phoneNo;
                                    one.photo = photoStr;
                                    contactslist.add(one);
                                }
                            }
                        }
                        pCur.close();
                    }
                }
            }
            if(cur!=null){
                cur.close();
            }
        } catch (Exception e){

        }

        contactsListAdapter.setDataList(contactslist);
        contactsListAdapter.notifyDataSetChanged();
    }

    private boolean checkNoExistList(String newphone){
        /*for(ContactModel one : contactslist){
            if(one.phone.equals(newphone)){
                return false;
            }
        }*/
        return true;
    }

    public static boolean checkPermission(final Context context)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED ) {

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
                    getContactList();
                } else {
                    //code for deny
                }
                break;
        }
    }
}
