package com.medx.networkplugin;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class UDPManagerPlugin extends Activity {

    private Context unityContext;
    private Activity unityActivity;
    private WifiManager mWifiManager;
    private WifiManager.MulticastLock multicastLock;
    private String message = null;

    public UDPManagerPlugin(Context unityContext, Activity unityActivity) {
        this.unityContext = unityContext;
        this.unityActivity = unityActivity;
        mWifiManager = (WifiManager)unityContext.getSystemService(Context.WIFI_SERVICE);
        initMulticastListener();
    }

    public void initMulticastListener(){
        multicastLock = mWifiManager.createMulticastLock("multicast");
        multicastLock.acquire();
        runThread();
    }

    public boolean isMulticastHeld(){
        return multicastLock.isHeld();
    }

    public String messageReceived(){
        String temp = message;
        message = null;
        return temp;
    }

    public void runThread() {
        new Thread() {
            public void run() {
                InetAddress ia = null;
                byte[] buffer = new byte[65535];
                MulticastSocket ms = null;
                int port = 2222;
                try {
                    ia = InetAddress.getByName("224.0.0.254");
                    DatagramPacket dp = new DatagramPacket(buffer, buffer.length, ia, port);
                    ms = new MulticastSocket(port);
                    ms.joinGroup(ia);
                    while (true) {
                        ms.receive(dp);
                        String s = new String(dp.getData(), 0, dp.getLength());
                        message = s;
                        Log.d("WifiTest", "run: " + s);
                    }
                } catch (UnknownHostException e) {
                    Log.d("WifiTest", "run: " + e.toString());
                } catch (IOException e) {
                    Log.d("WifiTest", "run: " + e.toString());
                }
            }
        }.start();
    }

}
