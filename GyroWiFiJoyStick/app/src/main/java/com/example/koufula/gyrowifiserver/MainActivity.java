package com.example.koufula.gyrowifiserver;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.example.koufula.util.SensorInfo;

public class MainActivity extends AppCompatActivity implements SensorInfoProvider{


    //private ServerThread serverThread;
    private MultiplexerServer serverThread;
    public static final String TAG = "GyroWiFiJoyStick";

    //陀螺仪相关
    private SensorManager mSensorManager;
    private Sensor mGyroscope;
    private Sensor mAccelerate;
    private OnSensorEventListener mOnSensorEventListener = new OnSensorEventListener();
    private OnAccSensorEventListener mOnAccSensorEventListener = new OnAccSensorEventListener();
    private float gyrox = 0, gyroy = 0, gyroz = 0;
    private float mAccX = 0, mAccY = 0, mAccZ = 0;

    private TextView statusView;
    private final int UPDATE_STATUS_VIEW_EVENT = 0;
    private Handler statusHandler = new Handler(){//线程与UI交互更新界面
        public void handleMessage(Message msg){
            int what = msg.what;
            Log.d(TAG, "in statusHandler what:" + what);
            switch (what) {
                case UPDATE_STATUS_VIEW_EVENT:
                if (statusView != null) {
                    String value = (String)msg.obj;
                    statusView.setText(value);
                }
            }

        }
    };

    //手动增加代码开始
    private View.OnClickListener myCloseOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (serverThread != null) {
                boolean result = serverThread.stop();
                if(result) {
                    Log.d(TAG, "in myOnClickListener, try to stop server");
                }
                else {
                    Log.d(TAG, "in myOnClickListener, server already stop");
                }
            } else {
                Log.d(TAG, "in myOnClickListener not running");
            }
        }
    };
    //手动增加代码结束

    private String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Button mStartBtn;
        Button mCloseBtn;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "in onCreate");

        mCloseBtn = (Button) findViewById(R.id.closeID);
        mCloseBtn.setOnClickListener(myCloseOnClickListener);
        statusView = (TextView) MainActivity.this.findViewById(R.id.statusID);

        //从系统服务中获得传感器管服务
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccelerate = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //注册传感器监听
        mSensorManager.registerListener(mOnSensorEventListener, mGyroscope, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mOnAccSensorEventListener, mAccelerate, SensorManager.SENSOR_DELAY_GAME);
        Log.d(TAG, "sensor registered");

        //获取wifi服务
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        TextView tv = (TextView) findViewById(R.id.ip);
        tv.setText("本机IP：" + ip);

        mStartBtn = (Button) this.findViewById(R.id.startID);
        //监听服务器开启
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (serverThread != null) {
                    boolean stopped = serverThread.isStopped();
                    if (!stopped) {
                        Log.d(TAG, "server already started");
                        return;
                    }
                }
                EditText portEditText = (EditText) MainActivity.this.findViewById(R.id.portID);
                String port = portEditText.getText().toString().trim();
                Log.d(TAG, "port is: " + port);
                if (port.equals("")) {
                    Log.d(TAG, "in if port is: " + port);
                    Toast.makeText(MainActivity.this, "port is not set", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "in else port is: " + port);
                    int p = Integer.parseInt(port);
                    serverThread = new MultiplexerServer(p, MainActivity.this);
                    new Thread(serverThread).start();
                    Toast.makeText(MainActivity.this, port, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*class ServerThread extends Thread {
        private ServerSocket ss;
        private Socket s;
        private OutputStream out;
        private int port;
        boolean mRunning;

        public ServerThread(int port) {
            this.port = port;
        }

        public void run() {
            //新建ServerSocket对象,端口为传进来的port;
            Message msg;
            mRunning = false;
            try {
                //ss= new ServerSocket(1821);
                ss = new ServerSocket(port);
                Log.d("GyroWIFI", "before accept");
                msg = statusHandler.obtainMessage(UPDATE_STATUS_VIEW_EVENT, "连接状态：等待连接");
                statusHandler.sendMessage(msg);
                s = ss.accept();
                Log.d("GyroWIFI", "after accept");
                msg = statusHandler.obtainMessage(UPDATE_STATUS_VIEW_EVENT, "连接状态：连接建立");
                statusHandler.sendMessage(msg);
                out = s.getOutputStream();
            } catch (Exception e) {
                //e.printStackTrace();
                Log.d("GyroWIFI", "accept failure");
                msg = statusHandler.obtainMessage(UPDATE_STATUS_VIEW_EVENT, "连接状态：建立失败");
                statusHandler.sendMessage(msg);
                return;
            }

            Log.d("GyroWIFI", "before send info");
            msg = statusHandler.obtainMessage(UPDATE_STATUS_VIEW_EVENT, "连接状态：发送数据中...");
            statusHandler.sendMessage(msg);
            mRunning = true;
            try {
                while (true) {
                    if (!mRunning)
                        break;
                    SensorInfo mSendbuffer = new SensorInfo(gyrox, gyroy, gyroz, mAccX, mAccY, mAccZ);
                    sendSensorInfo(mSendbuffer);

                    Thread.sleep(200);
                }
            } catch (Exception e) {
                Log.d("GyroWIFI", "send error");
                //e.printStackTrace();
            }
            Log.d("GyroWIFI", "send close");
            msg = statusHandler.obtainMessage(UPDATE_STATUS_VIEW_EVENT, "连接状态：连接关闭");
            statusHandler.sendMessage(msg);
        }

        public void sendSensorInfo(SensorInfo buffer) throws Exception {
            byte[] bytes;
            // object to bytearray
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(buffer);
            oo.flush();
            bytes = bo.toByteArray();
            out.write(bytes);
            out.flush();
            bo.close();
            oo.close();
        }
    }*/

    // 陀螺仪相关代码
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    public SensorInfo getSensorInfo() {
        SensorInfo sensorInfo = new SensorInfo(gyrox, gyroy, gyroz, mAccX, mAccY, mAccZ);
        return sensorInfo;
    }
    public void showgyroinfo() {
        showInfo("事件：" + " x:" + gyrox + " y:" + gyroy + " z:" + gyroz);
    }

    //陀螺仪非常敏感，平放在桌面，由于电脑照成的轻微震动在不断地刷屏，为了避免写UI造成的性能问题，只写Log。
    private void showInfo(String info) {
        //tv.append("\n" + info);
        Log.d("陀螺仪", info);
    }

    private String mTransformGyroInfo(float x, float y, float z) {
        String Gyroinfo = "Gyro:" + "X:" + x + "Y:" + y + "Z:" + z + "\n\r";
        return Gyroinfo;
    }

    private String mTransformAccInfo(float x, float y, float z) {
        String Accinfo = "Acc:" + "X:" + x + "Y:" + y + "Z:" + z + "\n\r";
        return Accinfo;
    }

    private class OnAccSensorEventListener implements SensorEventListener {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                //传感器获取值发生改变，在此处理
                //获得x轴的值
                mAccX = event.values[0];//手机横向翻滚
                //x>0 说明当前手机左翻 x<0右翻
                //获得y轴的值
                mAccY = event.values[1];//手机纵向翻滚
                //y>0 说明当前手机下翻 y<0上翻
                //获得z轴的值
                mAccZ = event.values[2]; //屏幕的朝向
                //z>0 手机屏幕朝上 z<0 手机屏幕朝下
                // Log.d("加速度仪","响应加速度仪事件");
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                Log.d("加速度仪", "OnAccSensorEventListener响应陀螺仪事件");
            }

        }
    }

    private class OnSensorEventListener implements SensorEventListener {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                //传感器获取值发生改变，在此处理
                //获得x轴的值
                gyrox = event.values[0];
                //获得y轴的值
                gyroy = event.values[1];
                //获得z轴的值
                gyroz = event.values[2];
                // Log.d("陀螺仪","响应陀螺仪事件");
            } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Log.d("陀螺仪", "OnSensorEventListener响应加速度仪事件");
            }

            //showgyroinfo();
            //Log.d("event","sensor changed");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //注销传感器监听
        mSensorManager.unregisterListener(mOnSensorEventListener, mGyroscope);
        mSensorManager.unregisterListener(mOnAccSensorEventListener, mAccelerate);
    }

}





