package com.caudelldevelopment.udacity.capstone.household.household.data;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by caude on 12/25/2017.
 */

public class User {

    private String name;
    private String id;
    private String family;
    private List<String> task_ids;

    public User() {
        task_ids = new LinkedList<>();
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
}
