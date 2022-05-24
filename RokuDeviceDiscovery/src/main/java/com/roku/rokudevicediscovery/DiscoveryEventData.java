package com.roku.rokudevicediscovery;

/**
 * Created by rburdick on 10/5/13.
 */
public class DiscoveryEventData {
    public MainActivity activity;
    public String url;
    public DiscoveryEventData(MainActivity _activity, String _url) {
        this.activity = _activity;
        this.url = _url;
    }
}
