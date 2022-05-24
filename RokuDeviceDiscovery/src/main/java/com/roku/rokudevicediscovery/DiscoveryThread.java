package com.roku.rokudevicediscovery;
import android.content.Intent;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;

/**
 * Created by rburdick on 10/5/13.
 */
public class DiscoveryThread extends Thread {
    private MainActivity client;
    private ThreadParams params;

    public DiscoveryThread(MainActivity client) {
        this.client = client;
    }

    public void setParams(ThreadParams params) {
        this.params = params;
    }

    public void discoverDevice() {
        try {
            int readCount = 0;
            String M_SEARCH = "M-SEARCH * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\nMAN: \"ssdp:discover\"\r\nMX: seconds to delay response\r\nST: urn:dial-multiscreen-org:service:dial:1\r\nUSER-AGENT: RokuCastClient";
            DatagramSocket clientSocket = new DatagramSocket();
            clientSocket.setSoTimeout(1000);
            InetAddress IPAddress = InetAddress.getByName("239.255.255.250");
            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];
            sendData = M_SEARCH.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 1900);
            clientSocket.send(sendPacket);
            String response = "";
            while (readCount < this.params.getIntData()) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    clientSocket.receive(receivePacket);
                    response = new String(receivePacket.getData());
                    String upnpLocation = this.client.getUPnPLocation(response);
                    //Move the following function to this thread class
                    String appUrl = this.client.sendDeviceDescriptionRequest(upnpLocation);
                    this.client.appUrls.add(appUrl);
                    Message msg = Message.obtain();
                    msg.what = MainActivity.DEVICE_FOUND_MESSAGE_ID;
                    msg.obj = new DiscoveryEventData(this.client, appUrl);
                    System.out.println("********************** appUrl ********************" + appUrl);
                    this.client.handler.sendMessage(msg);
                } catch (SocketTimeoutException ste) {
                }
                readCount++;
            }
            clientSocket.close();
            //Tell the main UI that the Discover button can now be enabled again
            Message uiMsg = Message.obtain();
            uiMsg.what = MainActivity.ENABLE_DISCOVER_MESSAGE_ID;
            this.client.handler.sendMessage(uiMsg);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void discoverApp() {
        try {
            String url = this.params.getUrl();
            URL obj = new URL(url + "/2DVideo");
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

            Intent intent = new Intent(this.client, AppInfoActivity.class);
            intent.putExtra("appInfo", res);
            Message uiMsg = Message.obtain();
            uiMsg.what = MainActivity.APP_DISCOVER_MESSAGE_ID;
            uiMsg.obj = intent;
            this.client.handler.sendMessage(uiMsg);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void run() {
        switch (this.params.getAction()) {
            case MainActivity.DISCOVER_DEVICE:
                this.discoverDevice();
                break;
            case MainActivity.DISCOVER_APP:
                this.discoverApp();
                break;
            default:
                break;
        }
    }

}
