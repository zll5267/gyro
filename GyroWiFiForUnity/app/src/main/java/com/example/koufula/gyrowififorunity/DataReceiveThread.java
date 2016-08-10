package com.example.koufula.gyrowififorunity;

import android.util.Log;

import com.example.koufula.util.SensorInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by ezhalel on 2016/8/10.
 */
public class DataReceiveThread implements Runnable {

    private InputStream in = null;
    private String mIp;
    private int mPort;
    private Socket s;
    private SensorInfo sensorInfo;
    private Object sensorInfoLock = new Object();
    private static String DEBUG_TAG = "GyroWifiForUnity";

    public DataReceiveThread(String ip, int port) {
        mIp = ip;
        mPort = port;
    }

    public SensorInfo getSensorInfo() {
        synchronized (sensorInfoLock) {
            return sensorInfo;
        }
    }
    public void run() {

        try {
            Log.d(DEBUG_TAG, "SocketClientControl ip address:" + mIp);
            Log.d(DEBUG_TAG, "SocketClientControl port:" + mPort);
            //s = new Socket(ip,port);
            s = new Socket();
            Log.d(DEBUG_TAG, "after create Socket" + s);
            s.connect(new InetSocketAddress(mIp, mPort), 5000);
            Log.d(DEBUG_TAG, "after connect Socket" + s);
            //获得链接
            in = s.getInputStream();
            //获得输入流
        } catch (UnknownHostException e) {
            Log.d(DEBUG_TAG, "UnknownHostException 重新获取");
            //e.printStackTrace();
            System.exit(-1);
        } catch (SocketException e) {
            Log.d(DEBUG_TAG, "SocketException 重新获取");
            //e.printStackTrace();
            System.exit(-1);
        } catch (SocketTimeoutException e) {
            Log.d(DEBUG_TAG, "SocketTimeoutException 重新获取");
            //e.printStackTrace();
            System.exit(-1);
        } catch (IOException e) {
            //e.printStackTrace();
            Log.d(DEBUG_TAG, "IOException 重新获取");
            System.exit(-1);
            //要是出问题，就线程退出
        }
        if (in != null) {
            new Thread(new ReadThread()).start(); //开启“读”线程
        }
    }
    private class ReadThread implements Runnable {
        byte[] bytes = new byte[1024];
        int i = 0;
        public void run() {
            while (true) {
                try {
                    Object obj = null;
                    int len = 0;
                    if ((len = in.read(bytes)) > 0) {
                        // bytearray to object
                        ByteArrayInputStream bi = new ByteArrayInputStream(bytes, 0, len);
                        ObjectInputStream oi = new ObjectInputStream(bi);
                        obj = oi.readObject();

                        synchronized (sensorInfoLock) {
                            sensorInfo = (SensorInfo) obj;
                        }
                        i++;
                        String result = "String("  + i + "):" + sensorInfo;
                        Log.d(DEBUG_TAG, result);
                        bi.close();
                        oi.close();
                    }
                } catch (IOException e) {
                    Log.d(DEBUG_TAG, e.getMessage());
                    //System.out.println("IOException" + e.getMessage());
                    //e.printStackTrace();
                } catch (Exception e) {
                    Log.d(DEBUG_TAG, e.getMessage());
                    //System.out.println("Exception" + e.getMessage());
                    //e.printStackTrace();
                }
            }
        }
    }
}
