package cz.org.shoosh.models;

import com.google.gson.annotations.SerializedName;

public class ContactModel {
    @SerializedName("name")
    public String name;

    @SerializedName("phone")
    public String phone;

    @SerializedName("photo")
    public String photo;
}
