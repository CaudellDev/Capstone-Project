package com.caudelldevelopment.udacity.capstone.household.household.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class User implements Parcelable {

    public static final String COL_TAG = "users";
    public static final String DOC_TAG = "user";

    public static final String NAME_ID = "name";
    public static final String FAMILY_ID = "family";
    public static final String TASKS_ID = "task_ids";

    private String name;
    private String id;
    private String family;

    public static User fromDoc(DocumentSnapshot doc) {
        User user = doc.toObject(User.class);
        user.setId(doc.getId());
        return user;
    }

    public static User fromSnapshot(DataSnapshot query) {
        User user = query.getValue(User.class);

        if (user != null) {
            user.setId(query.getKey());
        }

        return user;
    }

    // Required for Firebase DocumentSnapshot toObject function.
    public User() {
        family = "";
    }

    public User(FirebaseUser user) {
        id = user.getUid();
        name = user.getDisplayName();
    }

    private User(Parcel in) {
        name = in.readString();
        id = in.readString();
        family = in.readString();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();

        result.put(NAME_ID, name);
        result.put(FAMILY_ID, family);

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

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public boolean hasFamily() {
        return (family != null) && !family.isEmpty();
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
