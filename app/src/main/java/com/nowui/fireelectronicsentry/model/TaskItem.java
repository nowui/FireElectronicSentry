package com.nowui.fireelectronicsentry.model;

import com.nowui.fireelectronicsentry.utility.Helper;

import java.util.List;

/**
 * Created by yongqiangzhong on 8/4/15.
 */
public class TaskItem {

    private String patrolTaskExecutionId;

    private String deptPatrolID;

    private String taskitemid;

    private String taskitemname;

    private String taskitemstatus;

    private String taskItemPicture;

    private String taskItemPicture2;

    private String taskItemPicture3;

    private String taskItemPicture4;

    private String taskItemPicture5;

    private String taskItemDesc;

    private String taskItemAddress;

    private String taskItemAudio;

    private String taskItemSelect;

    public String getPatrolTaskExecutionId() {
        return patrolTaskExecutionId;
    }

    public void setPatrolTaskExecutionId(String patrolTaskExecutionId) {
        this.patrolTaskExecutionId = patrolTaskExecutionId;
    }

    public String getDeptPatrolID() {
        return deptPatrolID;
    }

    public void setDeptPatrolID(String deptPatrolID) {
        this.deptPatrolID = deptPatrolID;
    }

    private List<TaskAbnormal> taskAbnormalList;

    public String getTaskitemid() {
        return taskitemid;
    }

    public void setTaskitemid(String taskitemid) {
        this.taskitemid = taskitemid;
    }

    public String getTaskitemname() {
        return taskitemname;
    }

    public void setTaskitemname(String taskitemname) {
        this.taskitemname = taskitemname;
    }

    public String getTaskitemstatus() {
        return taskitemstatus;
    }

    public void setTaskitemstatus(String taskitemstatus) {
        this.taskitemstatus = taskitemstatus;
    }

    public String getTaskItemAudio() {
        return taskItemAudio;
    }

    public void setTaskItemAudio(String taskItemAudio) {
        this.taskItemAudio = taskItemAudio;
    }

    public String getTaskItemDesc() {
        return taskItemDesc;
    }

    public void setTaskItemDesc(String taskItemDesc) {
        this.taskItemDesc = taskItemDesc;
    }

    public String getTaskItemPicture() {
        return taskItemPicture;
    }

    public void setTaskItemPicture(String taskItemPicture) {
        this.taskItemPicture = taskItemPicture;
    }

    public String getTaskItemPicture2() {
        return taskItemPicture2;
    }

    public void setTaskItemPicture2(String taskItemPicture2) {
        this.taskItemPicture2 = taskItemPicture2;
    }

    public String getTaskItemPicture3() {
        return taskItemPicture3;
    }

    public void setTaskItemPicture3(String taskItemPicture3) {
        this.taskItemPicture3 = taskItemPicture3;
    }

    public String getTaskItemPicture4() {
        return taskItemPicture4;
    }

    public void setTaskItemPicture4(String taskItemPicture4) {
        this.taskItemPicture4 = taskItemPicture4;
    }

    public String getTaskItemPicture5() {
        return taskItemPicture5;
    }

    public void setTaskItemPicture5(String taskItemPicture5) {
        this.taskItemPicture5 = taskItemPicture5;
    }

    public List<TaskAbnormal> getTaskAbnormalList() {
        return taskAbnormalList;
    }

    public void setTaskAbnormalList(List<TaskAbnormal> taskAbnormalList) {
        this.taskAbnormalList = taskAbnormalList;
    }

    public String getTaskItemAddress() {
        if(Helper.isNullOrEmpty(taskItemAddress)) {
            return "";
        }
        return taskItemAddress;
    }

    public void setTaskItemAddress(String taskItemAddress) {
        this.taskItemAddress = taskItemAddress;
    }

    public String getTaskItemSelect() {
        if(Helper.isNullOrEmpty(taskItemSelect)) {
            return "";
        }
        return taskItemSelect;
    }

    public void setTaskItemSelect(String taskItemSelect) {
        this.taskItemSelect = taskItemSelect;
    }
}
