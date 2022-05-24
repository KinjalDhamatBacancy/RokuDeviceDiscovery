package com.roku.rokudevicediscovery;
import android.os.Message;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;

/**
 * Created by rburdick on 2/5/14.
 */
public class LaunchThread extends Thread {
    private MainActivity client;
    private LaunchThreadParams params;
    public LaunchThread(MainActivity client) {
        this.client = client;
    }

    public void setParams(LaunchThreadParams params) {
        this.params = params;
    }

    public void launchApp() {
        String appRunLocation = "";
        try {
            URL obj = new URL(client.appUrls.get(client.selectedAppUrl) + "/2DVideo");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add request header
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "text/plain; charset=\"utf-8\"");

            String urlParameters = "videoUrl=" + URLEncoder.encode("http://video.ted.com/talks/podcast/DavidKelley_2002_480.mp4", "UTF-8");
            urlParameters += "&streamFormat=mp4";
            urlParameters += "&play_start=" + this.params.getPlayStart();

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            appRunLocation = client.getHeader(con, MainActivity.RUN_LOCATION);
            Message msg = Message.obtain();
            msg.what = MainActivity.APP_LAUNCH_MESSAGE_ID;
            msg.obj = appRunLocation;
            this.client.handler.sendMessage(msg);
            in.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void stopApp() {
        try {
            URL obj = new URL(this.params.getUrl());
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            //add request header
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestMethod("DELETE");
            con.connect();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void run() {
        switch (this.params.getAction()) {
            case MainActivity.ACTION_LAUNCH:
                this.launchApp();
                break;
            case MainActivity.ACTION_STOP:
                this.stopApp();
                break;
            default:
                break;
        }
    }
}
