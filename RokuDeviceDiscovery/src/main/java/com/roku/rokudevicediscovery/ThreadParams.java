package com.roku.rokudevicediscovery;

/**
 * Created by rburdick on 2/5/14.
 */
public class ThreadParams {
    private int action;
    private int int_data;
    private String url;

    public ThreadParams() {}

    public int getAction() {
        return this.action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getIntData() {
        return this.int_data;
    }

    public void setIntData(int int_data) {
        this.int_data = int_data;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }
}
