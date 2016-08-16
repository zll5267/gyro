package com.example.koufula.gyrowififorunity;

import android.util.Log;
import com.example.koufula.util.SensorInfo;

/**
 * Created by ezhalel on 2016/8/16.
 */
public class Main {
    private static String DEBUG_TAG = "Gyro-Main";
    private float INVALID_VALUE = 0;
    private DataReceiveThread dataReceiveThread;

    public void startDataReceiveThread(String ipaddress, String port) {
        int iPort = Integer.parseInt(port);
        Log.d(DEBUG_TAG, "MainActivity: Start dataReceiveThread");
        Log.d(DEBUG_TAG, "Ip address is " +ipaddress );
        Log.d(DEBUG_TAG, "port is  " +port );
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
