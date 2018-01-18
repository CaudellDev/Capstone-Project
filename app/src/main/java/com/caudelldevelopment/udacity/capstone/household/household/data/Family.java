package com.caudelldevelopment.udacity.capstone.household.household.data;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by caude on 12/25/2017.
 */

public class Family {

    private String name;
    private String id;
    private List<String> task_ids;
    private List<String> members;

    public Family() {
        task_ids = new LinkedList<>();
        members = new LinkedList<>();
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

    public void setTask_ids(List<String> task_ids) {
        this.task_ids = task_ids;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }
}
