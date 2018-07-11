package com.hust.stormfury.diabetes.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by StormFury on 5/8/2018.
 */

public class Message implements Parcelable{
    private String message;
    private String date;
    private String message_id;
    private String user_id;

    public Message(){
    }

    public Message(String message, String date, String message_id, String user_id) {
        this.message = message;
        this.date = date;
        this.message_id = message_id;
        this.user_id = user_id;
    }

    protected Message(Parcel in) {
        message = in.readString();
        date = in.readString();
        message_id = in.readString();
        user_id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(message);
        dest.writeString(date);
        dest.writeString(message_id);
        dest.writeString(user_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
    public static Creator<Message> getCREATOR() {
        return CREATOR;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }
    public String getMessage_id() {
        return message_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_id() {
        return user_id;
    }


    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", date='" + date + '\'' +
                ", message_id='" + message_id + '\'' +
                ", user_id='" + user_id + '\'' +
                '}';
    }
}
