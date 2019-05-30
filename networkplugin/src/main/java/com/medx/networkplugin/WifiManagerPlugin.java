package com.medx.networkplugin;

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
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class WifiManagerPlugin extends Activity{

    private WifiManager mWifiManager;
    private List<ScanResult> mScanResults;
    private WifiConfiguration wifiConfig;
    private IntentFilter mIntentFilter;
    private Context unityContext;
    private Activity unityActivity;
    private Object hasResponse = null;
    private boolean conState;

    private final int MAX_STRENGHT = 5;
    private final String[] permissions = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    public WifiManagerPlugin(Context unityContext, Activity unityActivity) {
        this.unityContext = unityContext;
        this.unityActivity = unityActivity;
        mIntentFilter = new IntentFilter();
        mWifiManager = (WifiManager) unityContext.getApplicationContext().getSystemService(WIFI_SERVICE);
        CheckPermissions();
        Log.d("WifiTest", "onCreate: Lenght: ");
    }

    public void CheckPermissions(){
        Log.d("WifiTest", "On CheckPermissions");

        ArrayList<String> permissionsToRequest = new ArrayList<>();

        for (String permission : permissions) {
            if(unityContext.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
                permissionsToRequest.add(permission);
            }
        }

        Log.d("WifiTest", "Length " + permissionsToRequest.size());

        if(permissionsToRequest.size() != 0){
            String[] temp = new String[permissionsToRequest.size()];
            for (int i = 0; i < permissionsToRequest.size(); i++) {
                temp[i] = permissionsToRequest.get(i);
            }

            unityActivity.requestPermissions(temp, 1);
        }else{
            ScanForNetworks();
        }
    }

    public void ScanForNetworks(){
        Log.d("WifiTest", "On ScanForNetworks");

        if(!mWifiManager.isWifiEnabled()){
            Log.d("WifiTest", "ScanForNetworks: Setting wifi enabled");
            mWifiManager.setWifiEnabled(true);
        }

        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        unityActivity.registerReceiver(mScanReceiver, mIntentFilter);

        Log.d("WifiTest", "ScanForNetworks: Starting scanner");
        mWifiManager.startScan();
    }

    public String[] GetScanResults(){

        while (mScanResults == null){
            Log.d("WifiTest", "GetScanResults: Waiting for scan complete");
        }

        String[] mSSID = new  String[mScanResults.size()];

        for (int i = 0; i < mScanResults.size(); i++) {
            mSSID[i] = mScanResults.get(i).SSID + ";" + WifiManager.calculateSignalLevel(mScanResults.get(i).level, MAX_STRENGHT);
        }

        return mSSID;
    }

    public void ConnectToWifi(String SSID, String pass){

        mWifiManager.setWifiEnabled(false);
        mWifiManager.setWifiEnabled(true);

        String networkSSID = SSID;
        String networkPass = pass;

        wifiConfig = new WifiConfiguration();

        wifiConfig.SSID = String.format("\"%s\"", networkSSID);
        wifiConfig.preSharedKey = String.format("\"%s\"", networkPass);

        int netId = mWifiManager.addNetwork(wifiConfig);

        mWifiManager.disconnect();
        mWifiManager.enableNetwork(netId, true);
        mWifiManager.reconnect();
    }

    public boolean CheckIfConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) unityContext.getApplicationContext().getSystemService(unityContext.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnectedOrConnecting();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("WifiTest", "On CAllback");

        switch (requestCode){
            case 1: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    ScanForNetworks();
                }
            }
        }
    }

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){

                mScanResults = mWifiManager.getScanResults();
                Log.d("WifiTest", "onReceive: Lenght: " + mScanResults.size());

            }else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){

                NetworkInfo nwInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if(nwInfo.getDetailedState().equals(NetworkInfo.DetailedState.OBTAINING_IPADDR)){
                    conState = true;
                    hasResponse = new Object();
                    Log.d("WifiTest", "onReceive: Network state changed: " + nwInfo.getDetailedState().name());
                }

            }else if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){

                Log.d("WifiTest", "onReceive: Wifi state changed : Connected");
            }
        }
    };
}
