package com.caudelldevelopment.udacity.capstone.household.household.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by caude on 12/25/2017.
 */

public class User implements Parcelable {

    public static final String COL_TAG = "users";
    public static final String DOC_TAG = "user";

    private String name;
    private String id;
    private String family;
    private List<String> task_ids;

    public User() {
        task_ids = new LinkedList<>();
    }

    private User(Parcel in) {
        name = in.readString();
        id = in.readString();
        family = in.readString();
        task_ids = in.createStringArrayList();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public List<String> getTask_ids() {
        return task_ids;
    }

    public String getTask(int i) {
        return task_ids.get(i);
    }

    public void setTask_ids(List<String> task_ids) {
        this.task_ids = task_ids;
    }

    public void setTask(int pos, String id) {
        task_ids.set(pos, id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(name);
        out.writeString(id);
        out.writeString(family);
        out.writeStringList(task_ids);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {

        @Override
        public User createFromParcel(Parcel parcel) {
            return new User(parcel);
        }

        @Override
        public User[] newArray(int i) {
            return new User[i];
        }
    };
}
