package com.roku.rokudevicediscovery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

public class MainActivity extends Activity {
    public final static String LOCATION = "LOCATION:";
    public final static String RUN_LOCATION = "LOCATION";
    public final static String APPLICATION_URL = "Application-URL";
    public final static int DEVICE_FOUND_MESSAGE_ID = 1;
    public final static int ENABLE_DISCOVER_MESSAGE_ID = 2;
    public final static int APP_LAUNCH_MESSAGE_ID = 3;
    public final static int APP_DISCOVER_MESSAGE_ID = 4;
    public final static int DISCOVER_DEVICE = 0;
    public final static int DISCOVER_APP = 1;
    public final static int ACTION_LAUNCH = 0;
    public final static int ACTION_STOP = 1;

    public String appRunUrl;
    public List<String> appUrls;
    public List<String> listItems = new ArrayList<String>();
    public ArrayAdapter<String> appUrlListAdapter;
    private DiscoveryThread discoveryThread;
    private ThreadParams discoveryThreadParams;
    private LaunchThread launchThread;
    private LaunchThreadParams launchThreadParams;
    public int play_start;
    public int selectedAppUrl = 0;

    protected Button discoverButton;
    protected Button channelInfoButton;
    protected Button launchButton;
    protected Button resumeButton;
    protected Button stopButton;
    protected Button exitButton;
    protected Spinner appUrlList;

    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DEVICE_FOUND_MESSAGE_ID:
                    DiscoveryEventData ded = (DiscoveryEventData)msg.obj;
                    ded.activity.notifyAppUrlFound(ded.url);
                    channelInfoButton.setEnabled(true);
                    launchButton.setEnabled(true);
                    resumeButton.setEnabled(true);
                    break;
                case ENABLE_DISCOVER_MESSAGE_ID:
                    discoverButton.setEnabled(true);
                    break;
                case APP_LAUNCH_MESSAGE_ID:
                    appRunUrl = (String)msg.obj;
                    break;
                case APP_DISCOVER_MESSAGE_ID:
                    Intent intent = (Intent)msg.obj;
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        discoverButton = (Button)findViewById(R.id.buttonDiscovery);
        channelInfoButton = (Button)findViewById(R.id.buttonChannelInfo);
        launchButton = (Button)findViewById(R.id.buttonLaunch);
        resumeButton = (Button)findViewById(R.id.buttonResume);
        stopButton = (Button)findViewById(R.id.buttonStop);
        exitButton = (Button)findViewById(R.id.buttonExit);
        channelInfoButton.setEnabled(false);
        launchButton.setEnabled(false);
        resumeButton.setEnabled(false);
        stopButton.setEnabled(false);
        discoverButton.setOnClickListener(discoverListener);
        channelInfoButton.setOnClickListener(channelInfoListener);
        launchButton.setOnClickListener(launchListener);
        resumeButton.setOnClickListener(resumeListener);
        stopButton.setOnClickListener(stopListener);
        exitButton.setOnClickListener(exitListener);
        appUrlList = (Spinner)findViewById(R.id.appUrlList);
        appUrlListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, listItems);
        appUrlListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        appUrlList.setAdapter(appUrlListAdapter);
        appUrlList.setOnItemSelectedListener(appUrlSelectedListener);
        discoveryThreadParams = new ThreadParams();
        launchThreadParams = new LaunchThreadParams();
    }

    private View.OnClickListener discoverListener = new View.OnClickListener() {
        public void onClick(View v) {
            discoverButton.setEnabled(false);
            sendDiscoveryRequest();
        }
    };

    private View.OnClickListener channelInfoListener = new View.OnClickListener() {
        public void onClick(View v) {
            discoveryThreadParams.setAction(MainActivity.DISCOVER_APP);
            discoveryThreadParams.setUrl(appUrls.get(selectedAppUrl));
            discoveryThread = new DiscoveryThread(MainActivity.this);
            discoveryThread.setParams(discoveryThreadParams);
            discoveryThread.start();
        }
    };

    private View.OnClickListener launchListener = new View.OnClickListener() {
        public void onClick(View v) {
            launchThread = new LaunchThread(MainActivity.this);
            launchThreadParams.setAction(MainActivity.ACTION_LAUNCH);
            launchThreadParams.setPlayStart(0);
            launchThread.setParams(launchThreadParams);
            launchThread.start();
            launchButton.setEnabled(false);
            resumeButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
    };

    private View.OnClickListener resumeListener = new View.OnClickListener() {
        public void onClick(View v) {
            launchThread = new LaunchThread(MainActivity.this);
            launchThreadParams.setAction(MainActivity.ACTION_LAUNCH);
            launchThreadParams.setPlayStart(120);
            launchThread.setParams(launchThreadParams);
            launchThread.start();
            launchButton.setEnabled(false);
            resumeButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
    };

    private View.OnClickListener stopListener = new View.OnClickListener() {
        public void onClick(View v) {
            launchThread = new LaunchThread(MainActivity.this);
            launchThreadParams.setAction(MainActivity.ACTION_STOP);
            launchThreadParams.setUrl(appRunUrl);
            launchThread.setParams(launchThreadParams);
            launchThread.start();
            launchButton.setEnabled(true);
            resumeButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    };

    private View.OnClickListener exitListener = new View.OnClickListener() {
        public void onClick(View v) {
            MainActivity.this.finish();
        }
    };

    private AdapterView.OnItemSelectedListener appUrlSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            selectedAppUrl = i;
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void sendDiscoveryRequest() {
        appUrls = new java.util.ArrayList<String>();
        discoveryThreadParams.setAction(MainActivity.DISCOVER_DEVICE);
        discoveryThreadParams.setIntData(5);
        discoveryThread = new DiscoveryThread(MainActivity.this);
        discoveryThread.setParams(discoveryThreadParams);
        discoveryThread.start();
    }

    public String getUPnPLocation(String M_SEARCH_Response)
    {
        int start = M_SEARCH_Response.indexOf(LOCATION);
        start += LOCATION.length() + 1;
        String location = M_SEARCH_Response.substring(start);
        int end = location.indexOf('\r', 0);
        location = location.substring(0, end);
        return location;
    }

    public String sendDeviceDescriptionRequest(String upnpLocation) {
        String appUrl = "";
        try {
            URL obj = new URL(upnpLocation);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            appUrl = getHeader(con, APPLICATION_URL);
            in.close();

            //print result
            System.out.println(response.toString());
        } catch (Exception e) {
        }
        return appUrl;
    }

    public void sendChannelInfoRequest() {
        try {
            URL obj = new URL(appUrls.get(selectedAppUrl) + "/2DVideo");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            String res = response.toString();
            Log.d("App info response", res);

            Intent intent = new Intent(this, AppInfoActivity.class);
            intent.putExtra("appInfo", res);
            startActivity(intent);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public String getHeader(HttpURLConnection con, String header) {
        java.util.List<String> values = new java.util.ArrayList<String>();
        int idx = (con.getHeaderFieldKey(0) == null) ? 1 : 0;
        while (true) {
            String key = con.getHeaderFieldKey(idx);
            if (key == null)
                break;
            if (header.equalsIgnoreCase(key))
                return con.getHeaderField(idx);
            ++idx;
        }
        return "";
    }

    public void notifyAppUrlFound(String url) {
        try {
            System.out.println("URL" + url);
            listItems.add(url);
            appUrlListAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
