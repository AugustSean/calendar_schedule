package com.smile.calendar.module;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;



public class ModelTime extends RealmObject {
    private String earlyDate;
    private String lastDate;
    @PrimaryKey
    private String moduleName;

    public ModelTime() {

    }

    public ModelTime(String startTime, String endTime, String moduleName) {
        this.earlyDate = startTime;
        this.lastDate = endTime;
        this.moduleName = moduleName;
    }

    public String getEarlyDate() {
        return earlyDate;
    }

    public void setEarlyDate(String earlyDate) {
        this.earlyDate = earlyDate;
    }

    public String getLastDate() {
        return lastDate;
    }

    public void setLastDate(String lastDate) {
        this.lastDate = lastDate;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
}
