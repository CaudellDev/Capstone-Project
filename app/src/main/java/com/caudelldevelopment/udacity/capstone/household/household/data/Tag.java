package com.caudelldevelopment.udacity.capstone.household.household.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by caude on 12/25/2017.
 */
public class Tag {

    public static final String COL_TAG = "tags";
    public static final String DOC_TAG = "tag";

    public static final String NAME_ID = "name";
    public static final String TASKS_ID = "task_ids";

    private String name;
    private String id;
    private int count;
    private List<String> task_ids;

    public Tag() {
        task_ids = new LinkedList<>();
        count = 0;
    }

    public Tag(String name) {
        this.name = name;
        id = name.toLowerCase().replace(" ", "_");
        task_ids = new LinkedList<>();
        count = 0;
    }

    public Map<String, String> toMap() {
        Map<String, String> result = new HashMap<>();

        result.put(NAME_ID, name);

        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.id = name.toLowerCase().replace(" ", "_");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id.toLowerCase().replace(" ", "_");
    }

    public int getCount() {
        return count;
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
        count = task_ids.size();
    }

    public void removeTask(String task) {
        task_ids.remove(task);
        count = task_ids.size();
    }

    public void removeTask(int pos) {
        task_ids.remove(pos);
        count = task_ids.size();
    }
}
