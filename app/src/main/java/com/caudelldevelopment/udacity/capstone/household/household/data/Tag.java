package com.caudelldevelopment.udacity.capstone.household.household.data;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by caude on 12/25/2017.
 */
public class Tag {

    private String title;
    private String id;
    private int count;
    private List<String> task_ids;

    public Tag() {
        task_ids = new LinkedList<>();
        count = 0;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
