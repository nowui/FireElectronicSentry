package com.nowui.fireelectronicsentry.model;

import java.util.List;

/**
 * Created by yongqiangzhong on 9/12/15.
 */
public class TodayUnfinishedTask {

    private Integer patrolParentID;

    private String patrolParentItemName;

    private String unfinishedTaskCount;

    private List<Task> taskList;

    public Integer getPatrolParentID() {
        return patrolParentID;
    }

    public void setPatrolParentID(Integer patrolParentID) {
        this.patrolParentID = patrolParentID;
    }

    public String getPatrolParentItemName() {
        return patrolParentItemName;
    }

    public void setPatrolParentItemName(String patrolParentItemName) {
        this.patrolParentItemName = patrolParentItemName;
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    public String getUnfinishedTaskCount() {
        return unfinishedTaskCount;
    }

    public void setUnfinishedTaskCount(String unfinishedTaskCount) {
        this.unfinishedTaskCount = unfinishedTaskCount;
    }
}
