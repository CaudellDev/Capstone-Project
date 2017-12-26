package com.caudelldevelopment.udacity.capstone.household.household.data;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by caude on 12/25/2017.
 */

public class Family {

    private String name;
    private String id;
    private List<Task> task_ids;
    private List<User> user_ids;

    public Family() {
        task_ids = new LinkedList<>();
        user_ids = new LinkedList<>();
    }
}
