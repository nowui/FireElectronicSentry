package com.nowui.fireelectronicsentry.model;

/**
 * Created by yongqiangzhong on 8/3/15.
 */
public class Attendance {

    private Integer id;

    private Integer userID;

    private String userName;

    private String userPictureBase64;

    private Integer loginOrAttendance;

    private Integer loginOrAttendanceStatus;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLoginOrAttendance() {
        return loginOrAttendance;
    }

    public void setLoginOrAttendance(Integer loginOrAttendance) {
        this.loginOrAttendance = loginOrAttendance;
    }

    public Integer getLoginOrAttendanceStatus() {
        return loginOrAttendanceStatus;
    }

    public void setLoginOrAttendanceStatus(Integer loginOrAttendanceStatus) {
        this.loginOrAttendanceStatus = loginOrAttendanceStatus;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPictureBase64() {
        return userPictureBase64;
    }

    public void setUserPictureBase64(String userPictureBase64) {
        this.userPictureBase64 = userPictureBase64;
    }
}
