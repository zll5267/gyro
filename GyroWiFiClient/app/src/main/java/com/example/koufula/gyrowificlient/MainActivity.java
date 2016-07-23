package com.example.koufula.gyrowificlient;

import android.net.Uri;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import com.example.koufula.util.SensorInfo;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class MainActivity extends AppCompatActivity {
    private EditText ipEdit = null;
    private EditText portEdit = null;
    private EditText buffEdit = null;
    private Button startButton = null;
    private Button sendButton = null;
    private TextView receiveView = null;
    private Socket s = null;
    private byte[] receiveBuffer = new byte[1024];
    private String sendBuffer = new String();
    private String ip = null;
    private int port;
    private String portstring = null;
    private Thread clientThread = null;
    private int cmdCount = 0;
    private Handler handler = new Handler() {
        //线程与UI交互更新界面
        public void handleMessage(Message msg) {
            receiveView.setText(new String(receiveBuffer).trim());
            Arrays.fill(receiveBuffer, (byte) 0);
            //清空
        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.init();
        /*开启socket通信*/
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("GyroWIFI", "in ip or port onclick" + s);
                if (s == null) {
                    //这里要设置验证!!!!!!!!!
                    /*设定ip和port*/
                    ip = ipEdit.getText().toString();
                    Log.d("GyroWIFI", "IP address:" + ip);
                    portstring = portEdit.getText().toString().trim();
                    //Log.d("GyroWIFI","PORT:"+portstring );
                    if (portstring.equals("")) {
                        Log.d("GyroWIFI", "PORT:" + portstring);
                    } else {
                        /*开启socket线程*/
                        Log.d("GyroWIFI", "PORT:" + portstring);
                        port = Integer.parseInt(portstring);
                        Log.d("GyroWIFI", "start socket ip address:" + ip);
                        Log.d("GyroWIFI", "start socket port:" + port);
                        new Thread(new SocketClientControl(ip, port)).start();
                    }
                }
                Toast.makeText(MainActivity.this, "服务器连接成功", Toast.LENGTH_SHORT).show();
            }
        });
        /*发送数据*/
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (s != null) sendBuffer = buffEdit.getText().toString();
                Toast.makeText(MainActivity.this, "send -> " + sendBuffer, Toast.LENGTH_SHORT).show();
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void init() {
        startButton = (Button) this.findViewById(R.id.startID);
        sendButton = (Button) this.findViewById(R.id.sendID);
        ipEdit = (EditText) this.findViewById(R.id.ipID);
        portEdit = (EditText) this.findViewById(R.id.portID);
        buffEdit = (EditText) this.findViewById(R.id.buffID);
        receiveView = (TextView) this.findViewById(R.id.recieiveID);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.koufula.gyrowificlient/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.koufula.gyrowificlient/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class SocketClientControl implements Runnable {
        private InputStream in = null;
        private OutputStream out = null;
        private String mIp;
        private int mPort;

        public SocketClientControl() {

        }

        public SocketClientControl(String ip, int port) {
            mIp = ip;
            mPort = port;
        }

        public void run() {

            try {
                Log.d("GyroWIFI", "SocketClientControl ip address:" + mIp);
                Log.d("GyroWIFI", "SocketClientControl port:" + mPort);
                //s = new Socket(ip,port);
                s = new Socket();
                Log.d("GyroWIFI", "after create Socket" + s);
                s.connect(new InetSocketAddress(mIp, mPort), 5000);
                Log.d("GyroWIFI", "after connect Socket" + s);
                //获得链接
                in = s.getInputStream();
                //获得输入流
                out = s.getOutputStream();
                //获得输出流
            } catch (UnknownHostException e) {
                Log.d("GyroWIFI", "UnknownHostException 重新获取");
                e.printStackTrace();
                // System.exit(-1);
            } catch (SocketException e) {
                Log.d("GyroWIFI", "SocketException 重新获取");
                e.printStackTrace();
                //System.exit(-1);
            } catch (SocketTimeoutException e) {
                Log.d("GyroWIFI", "SocketTimeoutException 重新获取");
                e.printStackTrace();
                //System.exit(-1);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("GyroWIFI", "IOException 重新获取");
                //System.exit(-1);
                //要是出问题，就线程退出
            }
            if ((in != null) && (out != null)) {
                new Thread(new WriteThread()).start();
                //开启“写”线程
                new Thread(new ReadThread()).start();
                //开启“读”线程
            }
        }
        /*
        private class ReadThread implements Runnable{
            public void run() {
                while(true){
                    try {
                        if(in.read(receiveBuffer) > 0){

                            handler.sendEmptyMessage(0);
                            //发送信息，更新UI
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        */
        private class ReadThread implements Runnable {

            public void run() {
                while (true) {
                    try {
                        byte[] bytes = new byte[1024];
                        Object obj = null;
                        if (in.read(bytes) > 0) {

                            // bytearray to object

                            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
                            ObjectInputStream oi = new ObjectInputStream(bi);
                            obj = oi.readObject();

                            SensorInfo si = (SensorInfo)obj;
                            String result = "String" + si;
                            receiveBuffer = result.getBytes();
                            bi.close();
                            oi.close();

                            handler.sendEmptyMessage(0);
                            //发送信息，更新UI
                        }
                    } catch (IOException e) {
                        System.out.println("IOException" + e.getMessage());
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.out.println("Exception" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }

        private class WriteThread implements Runnable {
            public void run() {
                while (true) {
                    if (!sendBuffer.equals("")) {
                        try {
                            out.write(sendBuffer.getBytes());
                            //输出
                            out.flush();
                            //输出刷新缓冲
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        sendBuffer = "";
                    }
                }
            }
        }
    }

}
