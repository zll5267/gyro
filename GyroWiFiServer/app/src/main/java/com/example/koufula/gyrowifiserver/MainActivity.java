package com.example.koufula.gyrowifiserver;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import com.example.koufula.gyrowifiserver.SensorInfo;

public class MainActivity extends AppCompatActivity {
    private Button start = null;
    private EditText bufferText = null;
    private Button send = null;
    private ServerThread serverThread = null;
    private String sendBuffer = null;
    private String receiveBuffer = null;
    private TcpSocketServer tss = null;
    private TextView receiveView = null;
    public static final String TAG = "GyroWiFiServer";


    //陀螺仪相关
    private SensorManager mSensorManager;
    private Sensor mGyroscope;
    private Sensor mAccelerate;
    private OnSensorEventListener mOnSensorEventListener = new OnSensorEventListener();
    private OnAccSensorEventListener mOnAccSensorEventListener = new OnAccSensorEventListener();
    private float gyrox = 0,gyroy=0,gyroz=0;
    private float mAccX = 0,mAccY = 0,mAccZ = 0;
    private Handler mHandler;
    private boolean mRunning = true;
    private Button mBtn;

    private Handler handler = new Handler(){//线程与UI交互更新界面
        public void handleMessage(Message msg){
            receiveView.setText(receiveBuffer);
            Toast.makeText(MainActivity.this, receiveBuffer, Toast.LENGTH_SHORT).show();
        }
    };

    private String intToIp(int i) {
        return (i & 0xFF ) + "." + ((i>> 8 ) & 0xFF) + "." + ((i >> 16 ) & 0xFF) + "." + ( i >> 24 & 0xFF) ;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG,"in onCreate");
        //陀螺仪相关
        /*
        HandlerThread thread = new HandlerThread("MyHandlerThread");
        thread.start();//创建一个HandlerThread并启动它
        mHandler = new Handler(thread.getLooper());//使用HandlerThread的looper对象创建Handler，如果使用默认的构造方法，很有可能阻塞UI线程
        mHandler.post(mBackgroundRunnable);//将线程post到Handler中
        */

        mBtn = (Button)findViewById(R.id.closeID);
        mBtn.setOnClickListener(myOnClickListener);

        //从系统服务中获得传感器管服务
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccelerate = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //注册传感器监听
        mSensorManager.registerListener(mOnSensorEventListener, mGyroscope, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mOnAccSensorEventListener, mAccelerate, SensorManager.SENSOR_DELAY_GAME);

        Log.d(TAG,"sensor registered");

        //获取wifi服务
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        TextView tv=(TextView) findViewById(R.id.ip);
        tv.setText("本机IP："+ip);
        receiveView = (TextView)this.findViewById(R.id.receiveID);
        start = (Button) this.findViewById(R.id.startID);
        //监听服务器开启
        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if(serverThread == null){
                    EditText portEditText = (EditText)MainActivity.this.findViewById(R.id.portID);
                    String port = portEditText.getText().toString().trim();
                    Log.d(TAG,"port is: " + port);
                    if(port.equals("")){
                        Log.d(TAG,"in if port is: " + port);
                        Toast.makeText(MainActivity.this, port, Toast.LENGTH_SHORT).show();
                    }else {
                        Log.d(TAG,"in else port is: " + port);
                        serverThread = new ServerThread(port);
                        serverThread.start();
                        Toast.makeText(MainActivity.this, port, Toast.LENGTH_SHORT).show();
                    }
                 }
             }
        });

        send = (Button)this.findViewById(R.id.sendID);
        bufferText = (EditText)this.findViewById(R.id.bufferID);
        //监听发送信息
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                sendBuffer = bufferText.getText().toString().trim();
                if(sendBuffer != null)
                    //为了避免线程把它弄为 buffer = null;
                Toast.makeText(MainActivity.this, sendBuffer, Toast.LENGTH_SHORT).show();
            }
        });

    }

    class ServerThread extends Thread{ private int port;
        public ServerThread (String port){
            this.port = Integer.parseInt(port);
        }
        public void run(){
            //建立服务端
            if (tss == null) tss = new TcpSocketServer(this.port);
            new Thread(new WriteThread()).start();
            //开启“写”线程
            new Thread(new ReadThread()).start();
            //开启“读”线程
        }
        private class ReadThread implements Runnable{
            public void run(){
                while(true){
                    if((receiveBuffer = tss.getMessage()) != null){
                        //收到不为null的信息就发送出去
                        Log.d(TAG,receiveBuffer);
                        handler.sendEmptyMessage(0);
                    }
                }
            }
        }
        private class WriteThread implements Runnable{
            public void run(){
                while(true){
                    try {
                        //发送数据
                        if(sendBuffer != null){
                            //tss.sendMessage(1821,buffer);
                            tss.sendMessage(sendBuffer);
                            sendBuffer = null;
                            //清空，不让它连续发
                        }

                        SensorInfo mSendbuffer = new SensorInfo(gyrox,gyroy,gyroz,mAccX,mAccY,mAccZ);
                        tss.mSendMessage(mSendbuffer);
                        //tss.sendMessage(mTransformGyroInfo(gyrox,gyroy,gyroz));
                        Thread.sleep(200);
                        //tss.sendMessage(mTransformGyroInfo(gyrox,gyroy,gyroz) + mTransformAccInfo(mAccX,mAccY,mAccZ));
                        //Thread.sleep(200);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    public class TcpSocketServer {
        private ServerSocket ss =null;
        private Socket s = null;
        private OutputStream out = null;
        private InputStream in = null;
        private String receiveSocketBuffer = null;
        public TcpSocketServer(int port){
            //新建ServerSocket对象,端口为传进来的port;
            try {
                //ss= new ServerSocket(1821);
                Log.d("GyroWIFI","no");
                ss = new ServerSocket(port);
                Log.d("GyroWIFI","yes");
                s = ss.accept();
                Log.d("GyroWIFI","after accept");
                out = s.getOutputStream();
                in = s.getInputStream();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void sendMessage(String buffer)throws Exception{
            //新建Socket通信对象，接受客户端发来的请求accept();
            //Socket s = ss.accept();
            //创建输入流对象InputStream
            InputStream bais = new ByteArrayInputStream(buffer.getBytes());
            byte[] buff = new byte[1024];
            bais.read(buff);
            out.write(buff);
            out.flush();
        }
        public void mSendMessage(SensorInfo buffer)throws Exception {
            byte[] bytes = new byte[1024];
            try {
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
            } catch (IOException e) {
                System.out.println("IOException" + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception" + e.getMessage());
                e.printStackTrace();
            }
        }
        public String getMessage(){
            byte[] temp = new byte[1024];
            try{
                if(in.read(temp) > 0) {
                    return receiveSocketBuffer = new String(temp).trim();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            } return null;
        }
        public String receiveMessage(){
            return null;
        }
    }


    //手动增加代码开始
    private View.OnClickListener myOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG,"in myOnClickListener");
        }
    };
    //手动增加代码结束

    // 陀螺仪相关代码
    @Override
    protected void onResume() {
        super.onResume();
        mRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRunning = false;
    }
    public void showgyroinfo(){
        showInfo("事件：" + " x:" + gyrox + " y:" + gyroy + " z:" + gyroz);
    }
    //陀螺仪非常敏感，平放在桌面，由于电脑照成的轻微震动在不断地刷屏，为了避免写UI造成的性能问题，只写Log。
    private void showInfo(String info){
        //tv.append("\n" + info);
        Log.d("陀螺仪",info);
    }
    private String mTransformGyroInfo(float x,float y,float z)
    {
        String Gyroinfo = null;
        Gyroinfo = "Gyro:" + "X:" + x + "Y:" + y + "Z:" +z + "\n\r" ;
        return Gyroinfo;
    }
    private String mTransformAccInfo(float x,float y,float z)
    {
        String Accinfo = null;
        Accinfo = "Acc:" + "X:" + x + "Y:" + y + "Z:" +z + "\n\r" ;
        return Accinfo;
    }
    //实现耗时操作的线程

    Runnable mBackgroundRunnable = new Runnable() {

        @Override
        public void run() {
            //----------模拟耗时的操作，开始---------------
            Log.i(TAG, "thread running!");
            while(mRunning){
                //Log.i(TAG, "thread running!");
                try {
                    Thread.sleep(200);
                    //showgyroinfo();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //----------模拟耗时的操作，结束---------------
        }
    };

    private class OnAccSensorEventListener implements SensorEventListener{
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy)
        {
            // TODO Auto-generated method stub
        }
        @Override
        public void onSensorChanged(SensorEvent event)
        {
            // TODO Auto-generated method stub

            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
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
            }else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                Log.d("加速度仪","OnAccSensorEventListener响应陀螺仪事件");
            }

        }
    }
    private class OnSensorEventListener implements SensorEventListener
    {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy)
        {
            // TODO Auto-generated method stub
        }

        @Override
        public void onSensorChanged(SensorEvent event)
        {
            // TODO Auto-generated method stub


            if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                //传感器获取值发生改变，在此处理
                //获得x轴的值
                gyrox = event.values[0];
                //获得y轴的值
                gyroy = event.values[1];
                //获得z轴的值
                gyroz = event.values[2];
                // Log.d("陀螺仪","响应陀螺仪事件");
            }else if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Log.d("陀螺仪","OnSensorEventListener响应加速度仪事件");
            }

            //showgyroinfo();
            //Log.d("event","sensor changed");
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //注销传感器监听
        mSensorManager.unregisterListener(mOnSensorEventListener, mGyroscope);
        mSensorManager.unregisterListener(mOnAccSensorEventListener, mAccelerate);
        mHandler.removeCallbacks(mBackgroundRunnable);
    }

}





