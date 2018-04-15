package com.example.nayan.appanalysis2.model;

/**
 * Created by Dev on 1/9/2018.
 */

public class MContact {
    private int id;
    private String displayName,normilizedPhone,phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNormilizedPhone() {
        return normilizedPhone;
    }

    public void setNormilizedPhone(String normilizedPhone) {
        this.normilizedPhone = normilizedPhone;
    }
}
