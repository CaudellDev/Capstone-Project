package com.caudelldevelopment.udacity.capstone.household.household.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Tag implements Parcelable {

    private static final String LOG_TAG = Tag.class.getSimpleName();

    public static final String COL_TAG = "tags";
    public static final String DOC_TAG = "tag";

    public static final String NAME_ID = "name";
    public static final String TASKS_ID = "task_ids";

    private String name;
    private String id;
    private Map<String, String> task_ids;

    public static Tag fromSnapshot(DataSnapshot query) {
        Log.v(LOG_TAG, "fromSnapshot - query id: " + query.getKey());
        Tag tag = query.getValue(Tag.class);
        tag.setId(query.getKey());
        return tag;
    }

    // Required for Firebase DocumentSnapshot.toObject function.
    public Tag() {
        task_ids = new HashMap<>();
    }

    private Tag(Parcel in) {
        id = in.readString();
        name = in.readString();
        task_ids = new HashMap<>();
        in.readMap(task_ids, String.class.getClassLoader());
    }

    public Tag(String name) {
        this.name = name;
        task_ids = new HashMap<>();
    }

    public Tag(String name, Map<String, String> task_ids) {
        this.name = name;
        this.task_ids = task_ids;
    }

    // If the tag doesn't have any task ids in Firebase, it'll be an empty string.
    // I need this here to catch those and init with an empty map.
    public Tag(String name, String empty_task_ids_placeholder) {
        this.name = name;
        task_ids = new HashMap<>();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();

        result.put(NAME_ID, name);
        result.put(TASKS_ID, task_ids);

        return result;
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

    public Map<String, String> getTask_ids() {
        return task_ids;
    }

    public void addTask(String task_id, String task_name) {
        task_ids.put(task_id, task_name);
    }

    public void removeTask(String task) {
        task_ids.remove(task);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tag) {
            Tag check = (Tag) obj;
            String check_id = check.getId();
            return (id == null) ? (check_id == null) : id.equals(check_id);
        } else {
            return false;
        }
    }

    // ###----- Parcelable -----###

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeMap(task_ids);
    }

    public static final Parcelable.Creator<Tag> CREATOR = new Parcelable.Creator<Tag>() {
        @Override
        public Tag createFromParcel(Parcel source) {
            return new Tag(source);
        }

        @Override
        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };
}
