package cz.org.shoosh.models;

import com.google.gson.annotations.SerializedName;

public class FeedModel {

    @SerializedName("id")
    public int key;

    @SerializedName("key")
    public String skey;

    @SerializedName("stime")
    public String stime;

    @SerializedName("name")
    public String uname;

    @SerializedName("phone")
    public String phoneno;

    @SerializedName("uid")
    public String uid;
}
