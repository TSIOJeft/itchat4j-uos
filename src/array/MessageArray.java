package com.farplace.farpush.array;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MessageArray implements Serializable {
    @SerializedName("content")
    public String msgContent;
    @SerializedName("nickName")
    public String userName;
    @SerializedName("photo")
    public String userPhoto;
    @SerializedName("userID")
    public String userID;
    @SerializedName("Msg")
    public String msgID;
}
