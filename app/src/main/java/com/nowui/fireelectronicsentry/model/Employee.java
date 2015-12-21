package com.nowui.fireelectronicsentry.model;

import com.nowui.fireelectronicsentry.utility.Helper;

/**
 * Created by yongqiangzhong on 7/26/15.
 */
public class Employee {

    private Integer id;

    private String name;

    private String pictureUrl;

    private String userType;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPictureUrl() {
        return Helper.WebUrl + pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
