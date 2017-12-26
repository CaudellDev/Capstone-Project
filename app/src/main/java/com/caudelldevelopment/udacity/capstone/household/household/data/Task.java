package com.caudelldevelopment.udacity.capstone.household.household.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by caude on 12/25/2017.
 */

public class Task implements Parcelable {

    private java.lang.String id;
    private java.lang.String user_id;
    private java.lang.String title;
    private java.lang.String desc;
    private java.lang.String date;
    private boolean complete;
    private boolean family;
    private List<String> tag_ids;

    public Task() {
        tag_ids = new LinkedList<>();
        complete = false;
        family = false;
    }

    public Task(Parcel in) {
        tag_ids = new LinkedList<>();

        id = in.readString();
        user_id = in.readString();
        title = in.readString();
        desc = in.readString();
        date = in.readString();
        family = in.readByte() == 1;
        in.readStringList(tag_ids);
    }

    public java.lang.String getId() {
        return id;
    }
    public void setId(java.lang.String id) {
        this.id = id;
    }

    public java.lang.String getUser_id() {
        return user_id;
    }
    public void setUser_id(java.lang.String id) {
        user_id = id;
    }

    public java.lang.String getTitle() {
        return title;
    }
    public void setTitle(java.lang.String title) {
        this.title = title;
    }

    public java.lang.String getDesc() {
        return desc;
    }
    public void setDesc(java.lang.String desc) {
        this.desc = desc;
    }

    public java.lang.String getDate() {
        return date;
    }
    public void setDate(java.lang.String date) {
        this.date = date;
    }

    public boolean isComplete() {
        return complete;
    }
    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isFamily() {
        return family;
    }
    public void setFamily(boolean family) {
        this.family = family;
    }

    public List<String> getTag_ids() {
        return tag_ids;
    }
    public String getTag(int pos) {
        return tag_ids.get(pos);
    }

    public void setTag_ids(List<String> tag_ids) {
        this.tag_ids = tag_ids;
    }
    public void setTag_id(int pos, String tag) {
        this.tag_ids.set(pos, tag);
    }

    public void addTag_id(String tag) {
        tag_ids.add(tag);
    }
    public void removeTag_id(int pos) {
        tag_ids.remove(pos);
    }
    public void removeTag_id(String tag) {
        tag_ids.remove(tag);
    }

    // ###----- Parcelable ----###

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(id);
        out.writeString(user_id);
        out.writeString(title);
        out.writeString(desc);
        out.writeString(date);
        out.writeByte((byte) (family ? 1 : 0));

        String[] tag_arr = new String[tag_ids.size()];
        out.writeArray(tag_ids.toArray(tag_arr));
    }

    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {

        @Override
        public Task createFromParcel(Parcel parcel) {
            return new Task(parcel);
        }

        @Override
        public Task[] newArray(int i) {
            return new Task[i];
        }
    };
}
