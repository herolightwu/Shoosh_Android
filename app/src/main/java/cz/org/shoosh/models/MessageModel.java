package cz.org.shoosh.models;

import com.google.gson.annotations.SerializedName;

public class MessageModel {

    @SerializedName("key")
    public String key;

    @SerializedName("sname")
    public String sname;

    @SerializedName("sid")
    public String sid;

    @SerializedName("msg")
    public String msg;

    @SerializedName("stime")
    public String stime;

}
