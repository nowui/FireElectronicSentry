package com.nowui.fireelectronicsentry.model;

/**
 * Created by yongqiangzhong on 8/4/15.
 */
public class Task {

    private String taskid;

    private String taskname;

    private String taskstart;

    private String taskend;

    private String subDeptID;

    private String subDeptName;

    public String getTaskid() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    public String getTaskname() {
        return taskname;
    }

    public void setTaskname(String taskname) {
        this.taskname = taskname;
    }

    public String getTaskend() {
        return taskend;
    }

    public void setTaskend(String taskend) {
        this.taskend = taskend;
    }

    public String getTaskstart() {
        return taskstart;
    }

    public void setTaskstart(String taskstart) {
        this.taskstart = taskstart;
    }

    public String getSubDeptID() {
        return subDeptID;
    }

    public void setSubDeptID(String subDeptID) {
        this.subDeptID = subDeptID;
    }

    public String getSubDeptName() {
        return subDeptName;
    }

    public void setSubDeptName(String subDeptName) {
        this.subDeptName = subDeptName;
    }
}
