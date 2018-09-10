package cz.org.shoosh.models;

import com.google.gson.annotations.SerializedName;

public class ThreadModel {
    @SerializedName("id")
    public int key;

    @SerializedName("key")
    public String skey;

    @SerializedName("sname")
    public String sname;

    @SerializedName("sid")
    public String sid;

    @SerializedName("msg")
    public String msg;

    @SerializedName("stime")
    public String stime;

    @SerializedName("sphone")
    public String sphone;
}
