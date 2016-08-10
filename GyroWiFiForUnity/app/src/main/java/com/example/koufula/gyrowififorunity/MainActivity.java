package com.example.koufula.gyrowififorunity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.example.koufula.util.SensorInfo;
import com.unity3d.player.UnityPlayerActivity;

public class MainActivity extends UnityPlayerActivity {

    private static String DEBUG_TAG = "GyroWifiForUnity";
    private float INVALID_VALUE = 0;

    private DataReceiveThread dataReceiveThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
    }
    public void startDataReceiveThread(String ipaddress, String port) {
        int iPort = Integer.parseInt(port);
        dataReceiveThread = new DataReceiveThread(ipaddress, iPort);
        new Thread(dataReceiveThread).start();
    }

    public float getSensorInfoX() {
        if (dataReceiveThread != null) {
            SensorInfo si = dataReceiveThread.getSensorInfo();
            if (si != null) {
                Log.d(DEBUG_TAG, "MainActivity: GyroX is:" + si.getmGyroX());
                return si.getmGyroX();
            }
            else {
                Log.d(DEBUG_TAG, "MainActivity: SensorInfo is NULL");
                return INVALID_VALUE;
            }
        }
        else {
            Log.d(DEBUG_TAG, "MainActivity: dataReceiveThread is NULL");
            return INVALID_VALUE;
        }
    }
}
