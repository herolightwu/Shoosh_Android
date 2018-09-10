package cz.org.shoosh.models;

import com.google.gson.annotations.SerializedName;

public class UserModel {
    @SerializedName("uname")
    public String uname;

    @SerializedName("UID")
    public String uid;

    @SerializedName("token")
    public String token;

    @SerializedName("devtype")
    public String devtype;

    @SerializedName("phoneno")
    public String phoneno;

    @SerializedName("hideme")
    public boolean bHide;

    @SerializedName("noti_set")
    public boolean bNoti;

    @SerializedName("contactlist")
    public String contactlist;

}
