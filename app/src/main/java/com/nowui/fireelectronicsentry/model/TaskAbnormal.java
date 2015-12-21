package com.nowui.fireelectronicsentry.model;

import java.io.Serializable;

/**
 * Created by yongqiangzhong on 8/12/15.
 */
public class TaskAbnormal implements Serializable {

    private String abnormalid;

    private String abnormaltype;

    private String abnormalname;

    public String getAbnormalid() {
        return abnormalid;
    }

    public void setAbnormalid(String abnormalid) {
        this.abnormalid = abnormalid;
    }

    public String getAbnormaltype() {
        return abnormaltype;
    }

    public void setAbnormaltype(String abnormaltype) {
        this.abnormaltype = abnormaltype;
    }

    public String getAbnormalname() {
        return abnormalname;
    }

    public void setAbnormalname(String abnormalname) {
        this.abnormalname = abnormalname;
    }
}
