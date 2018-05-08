package com.caudelldevelopment.udacity.capstone.household.household.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by caude on 12/25/2017.
 */

public class Family implements Parcelable {

    private static final String LOG_TAG = Family.class.getSimpleName();

    public static final String COL_TAG = "families";
    public static final String DOC_TAG = "family";

    public static final String NAME_ID = "name";
    public static final String MEMBERS_ID = "members";

    private String name;
    private String id;
    private Map<String, String> members;

    public static Family fromDoc(DocumentSnapshot doc) {
        Family family = doc.toObject(Family.class);
        family.setId(doc.getId());
        return family;
    }

    public static Family fromSnapshot(DataSnapshot query) {
        Family family = query.getValue(Family.class);

        if (family != null) {
            family.setId(query.getKey());
        }

        return family;
    }

    // Required for Firebase DocumentSnapshot toObject function.
    public Family() {
        members = new HashMap<>();
    }

    public Family(String name, Map<String, String> members) {
        this.name = name;
        this.members = members;
    }

    public Family(String name, User user) {
        this.name = name;
        members = new HashMap<>();
        members.put(user.getId(), user.getName());
    }

    public Family(Parcel in) {
        id = in.readString();
        name = in.readString();

        members = new HashMap<>();
        in.readMap(members, String.class.getClassLoader());
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();

        result.put(NAME_ID, name);
        result.put(MEMBERS_ID, members);

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

    public Map<String, String> getMembers() {
        return members;
    }

    public void removeMember(String member) {
        members.remove(member);
    }

    public void addMember(String member_id, String member_name) {
        members.put(member_id, member_name);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Family) {
            Family temp = (Family) o;
            return id.equals(temp.getId());
        } else {
            return false;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeMap(members);
    }

    public static final Parcelable.Creator<Family> CREATOR = new Parcelable.Creator<Family>() {

        @Override
        public Family createFromParcel(Parcel source) {
            return new Family(source);
        }

        @Override
        public Family[] newArray(int size) {
            return new Family[size];
        }
    };
}
