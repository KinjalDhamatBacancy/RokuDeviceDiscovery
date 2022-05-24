package com.roku.rokudevicediscovery;

/**
 * Created by rburdick on 2/5/14.
 */

public class LaunchThreadParams {
    private int action;
    private int play_start;
    private String url;

    public LaunchThreadParams() {
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getAction() {
        return this.action;
    }

    public void setPlayStart(int play_start) {
        this.play_start = play_start;
    }

    public int getPlayStart() {
        return this.play_start;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }
}
