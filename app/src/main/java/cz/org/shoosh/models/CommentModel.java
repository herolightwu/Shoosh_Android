package cz.org.shoosh.models;

import com.google.gson.annotations.SerializedName;

public class CommentModel {
    @SerializedName("key")
    public String mkey;

    @SerializedName("uid")
    public String uid;

    @SerializedName("name")
    public String name;

    @SerializedName("feedtime")
    public String feedtime;

    @SerializedName("content")
    public String content;

    @SerializedName("uphone")
    public String uphone;

    public boolean bDisp;
    public boolean bContact;
    public boolean breport;
}
