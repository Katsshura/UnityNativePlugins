package com.medx.unitynativeplugins;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        unityActivity = this;
        unityContext = this;
        mWifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        initMulticastListener();
        Log.d("WifiTest", "onCreate: Lenght: ");
    }

    @Override
    protected void onStart() {
        super.onStart();
        runThread();
    }

    private Context unityContext;
    private Activity unityActivity;
    private WifiManager mWifiManager;
    private WifiManager.MulticastLock multicastLock;
    private String message = null;

    public void initMulticastListener(){
        multicastLock = mWifiManager.createMulticastLock("multicast");
        multicastLock.acquire();
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
