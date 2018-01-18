package com.caudelldevelopment.udacity.capstone.household.household.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by caude on 12/25/2017.
 */

public class Task implements Parcelable {

    private static final String LOG_TAG = Task.class.getSimpleName();

    public static final String TASKS_ID = "tasks";
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
    private List<String> tag_ids;

    public Task() {
        tag_ids = new LinkedList<>();
        complete = false;
        family = false;
    }

    public Task(DocumentSnapshot doc) {
        try {
            id = doc.getId();
            access_id = doc.getString(ACCESS_ID);
            name = doc.getString("name");
            desc = doc.getString("desc");
            date = doc.getDate("date").toString();
            complete = doc.getBoolean("complete");
            family = doc.getBoolean("family");

            tag_ids = new LinkedList<>();

//            Object temp = doc.get("tag_ids");
//            LinkedList<String> temp_list = (LinkedList<String>) temp;
//
//            if (temp_list != null) {
//                tag_ids.addAll(temp_list);
//                Log.v(LOG_TAG, "new Task(DocumentSnapshot) - tag_ids have been added. Count: " + tag_ids.size());
//            } else {
//                Log.w(LOG_TAG, "new Task(DocumentSnapshot) -  casting doc.get(\"tag_ids\" to String list returns null!!!!");
//            }

            // Work on Tag reference conversion. Should I change Firebase to store an array of Strings?
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public Task(Parcel in) {
        tag_ids = new LinkedList<>();

        id = in.readString();
        access_id = in.readString();
        name = in.readString();
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

    public java.lang.String getAccess_id() {
        return access_id;
    }
    public void setAccess_id(java.lang.String id) {
        access_id = id;
    }

    public java.lang.String getName() {
        return name;
    }
    public void setName(java.lang.String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }
    public void setDesc(java.lang.String desc) {
        this.desc = desc;
    }

    public String getDateStr() {
        return date;
    }

    public Date getDate() {
        Date result = null;
        try {
            result = new SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void setDateStr(String date) {
        this.date = date;
    }
    public void setDate(Date date) { this.date = date.toString(); }

    public boolean isComplete() {
        return complete;
    }

    // Possibly required by Firebase Firestore
//    public boolean getComplete() {
//        return complete;
//    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isFamily() {
        return family;
    }

//    public boolean getFamily() {
//        return family;
//    }

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
        out.writeString(access_id);
        out.writeString(name);
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
