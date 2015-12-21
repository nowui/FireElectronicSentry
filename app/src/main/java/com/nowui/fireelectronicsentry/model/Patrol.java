package com.nowui.fireelectronicsentry.model;

/**
 * Created by yongqiangzhong on 9/11/15.
 */
public class Patrol {

    private String PatrolName;

    private String Longitude;

    private String Latitude;

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getPatrolName() {
        return PatrolName;
    }

    public void setPatrolName(String patrolName) {
        PatrolName = patrolName;
    }
}
