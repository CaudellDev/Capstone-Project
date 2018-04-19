package com.caudelldevelopment.udacity.capstone.household.household.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by caude on 12/25/2017.
 */
public class Tag implements Parcelable {

    private static final String LOG_TAG = Tag.class.getSimpleName();

    public static final String COL_TAG = "tags";
    public static final String DOC_TAG = "tag";

    public static final String NAME_ID = "name";
    public static final String TASKS_ID = "task_ids";

    private String name;
    private String id;
//    private int count;
    private List<String> task_ids;

    public static Tag fromDoc(DocumentSnapshot doc) {
        Tag tag = doc.toObject(Tag.class);
        tag.setId(doc.getId());
        return tag;
    }

    public Tag() {
        task_ids = new LinkedList<>();
    }

    private Tag(Parcel in) {
        id = in.readString();
        name = in.readString();
        task_ids = new LinkedList<>();
        in.readStringList(task_ids);
    }

    public Tag(String name) {
        this.name = name;
        task_ids = new LinkedList<>();
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

    public int getCount() {
        return task_ids.size();
    }

    public List<String> getTask_ids() {
        return task_ids;
    }

    public String getTask(int pos) {
        return task_ids.get(pos);
    }

    public void setTask_ids(List<String> task_ids) {
        this.task_ids = task_ids;
    }

    public void setTask(int pos, String task) {
        task_ids.set(pos, task);
    }

    public void addTask(String task) {
        task_ids.add(task);
    }

    public void removeTask(String task) {
        task_ids.remove(task);
    }

    public void removeTask(int pos) {
        task_ids.remove(pos);
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
        dest.writeStringList(task_ids);
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
