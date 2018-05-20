package com.caudelldevelopment.udacity.capstone.household.household.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by caude on 12/25/2017.
 */

public class Task implements Parcelable, Comparable<Task> {

    private static final String LOG_TAG = Task.class.getSimpleName();

    public static final String COL_TAG = "tasks";
    public static final String DOC_TAG = "task";

    public static final String TAG = "task_tag";
    public static final String ID = "id";
    public static final String ACCESS_ID = "access_id";
    public static final String COMP_ID = "complete";
    public static final String DATE_ID = "date";
    public static final String DESC_ID = "desc";
    public static final String FAM_ID = "family";
    public static final String NAME_ID = "name";
    public static final String TAGS_ID = "tag_ids";

    private String id;
    private String access_id;
    private String name;
    private String desc;
    private String date;
    private boolean complete;
    private boolean family;
//    private List<String> tag_ids;
    private Map<String, String> tag_ids;

    public static Task fromDoc(DocumentSnapshot doc, User user) {
        Task task = doc.toObject(Task.class);
        task.setId(doc.getId());

        if (task.isFamily()) {
            task.setAccess_id(user.getFamily());
        } else {
            task.setAccess_id(user.getId());
        }

        return task;
    }

    public static Task fromSnapshot(DataSnapshot query) {
        Task task = query.getValue(Task.class);
        task.setId(query.getKey());

        return task;
    }

    public static List<Task> convertParcelableArray(Parcelable[] arr) {
        if (arr != null) {
            Task[] task_arr = Arrays.copyOf(arr, arr.length, Task[].class);
            return new LinkedList<>(Arrays.asList(task_arr));
        }

        return new LinkedList<>();
    }

    // Required for Firebase DocumentSnapshot toObject function.
    public Task() {
        tag_ids = new HashMap<>();
        complete = false;
        family = false;
    }

    private Task(Parcel in) {
        tag_ids = new HashMap<>();

        id = in.readString();
        access_id = in.readString();
        name = in.readString();
        desc = in.readString();
        date = in.readString();
        family = in.readByte() == 1;
        in.readMap(tag_ids, String.class.getClassLoader());
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        map.put(ACCESS_ID, access_id);
        map.put(NAME_ID, name);
        map.put(DESC_ID, desc);
        map.put(COMP_ID, complete);
        map.put(FAM_ID, family);
        map.put(DATE_ID, date);
        map.put(TAGS_ID, tag_ids);

        return map;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getAccess_id() {
        return access_id;
    }
    public void setAccess_id(String id) {
        access_id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
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

    public Map<String, String> getTag_ids() {
        return tag_ids;
    }

    public void addTag_id(String tag_id, String tag_name) {
        tag_ids.put(tag_id, tag_name);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Task) {
            Task temp = (Task) o;
            return (id == null && temp.getId() == null) || id.equals(temp.getId());
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(@NonNull Task task) {
        return date.compareTo(task.date);
    }

    // ###----- Parcelable ----###

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(id);
        out.writeString(access_id);
        out.writeString(name);
        out.writeString(desc);
        out.writeString(date);
        out.writeByte((byte) (family ? 1 : 0));
        out.writeMap(tag_ids);
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
