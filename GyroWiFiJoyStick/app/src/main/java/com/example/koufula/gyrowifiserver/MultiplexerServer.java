package com.example.koufula.gyrowifiserver;

import com.example.koufula.util.SensorInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by ezhalel on 2016/8/23.
 */
public class MultiplexerServer implements Runnable{

    private Selector selector;
    private ServerSocketChannel servChannel;
    private volatile boolean stop;
    private boolean connected;// now we only support one client
    private SocketChannel connectChannel;//the connection with the client
    private final String TAG = "MultiplexerServer";

    SensorInfoProvider sensorInfoProvider;
    Handler uiHandler;

    public MultiplexerServer(int port, SensorInfoProvider provider, Handler handler) {
        try {
            selector = Selector.open();
            servChannel = ServerSocketChannel.open();
            servChannel.configureBlocking(false);
            servChannel.socket().bind(new InetSocketAddress(port), 10);
            servChannel.register(selector, SelectionKey.OP_ACCEPT);
            sensorInfoProvider = provider;
            uiHandler = handler;
            //System.out.println("the server is start on port:" + port);
            //Log.d(TAG, "the server uihandle:" + handler);
            Log.d(TAG, "the server is start on port:" + port);
        } catch (IOException e) {
            //e.printStackTrace();
            //System.exit(1);
            Log.d(TAG, "MultiplexerServer() catch IOException:" + e);
        }
    }

    /*
        @return false: means already stop, true means try to stop
     */
    public boolean stop() {
        if(this.stop) {
            return false;
        }
        this.stop = true;
        return true;
    }

    public boolean isStopped() {
        return this.stop;
    }

    @Override
    public void run() {
        connected = false;
        updateUI("连接状态：等待连接");
        while(!stop) {
            try {
                if (!connected) {
                    Log.d(TAG, "waiting connecting ..." + uiHandler);

                    selector.select(1000);
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> it = selectedKeys.iterator();
                    SelectionKey key = null;
                    while (it.hasNext()) {
                        key = it.next();
                        it.remove();
                        try {
                            if (key.isValid()) {
                                //new connection
                                if (key.isAcceptable()) {
                                    ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                                    connectChannel = ssc.accept();
                                    connectChannel.configureBlocking(false);
                                    connected = true;
                                    //connectChannel.register(selector, SelectionKey.OP_READ);
                                    updateUI("连接状态：连接成功");
                                }
                            }
                        } catch (Exception e) {
                            if (key != null) {
                                key.cancel();
                                if (key.channel() != null)
                                    key.channel().close();
                                ;
                            }
                            this.stop = true;
                            updateUI("连接状态：异常关闭");
                        }
                    }
                } else {
                    SensorInfo sensorInfo = sensorInfoProvider.getSensorInfo();
                    byte[] bytes;
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    ObjectOutputStream oo = new ObjectOutputStream(bo);
                    oo.writeObject(sensorInfo);
                    oo.flush();
                    bytes = bo.toByteArray();
                    bo.close();
                    oo.close();
                    ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
                    writeBuffer.put(bytes);
                    writeBuffer.flip();
                    connectChannel.write(writeBuffer);
                    //System.out.print("send sensorInfo:" + sensorInfo);
                    Log.d(TAG, "send sensorInfo:" + sensorInfo);
                    Thread.sleep(200);
                }
            } catch (Exception e) {
                //e.printStackTrace();
                Log.d(TAG, "run() catch Exception" + e);
                this.stop = true;
                updateUI("连接状态：异常关闭");
            }
        }

        if (selector != null) {
            try {
                if (servChannel != null) {
                    servChannel.close();
                }
                selector.close();
            } catch (IOException e) {
                //e.printStackTrace();;
                Log.d(TAG, "close catch IOException" + e);
            }
        }
        updateUI("连接状态：连接关闭");
    }

    private void updateUI(String info){
        if (uiHandler != null) {
            Message msg = uiHandler.obtainMessage(MainActivity.UPDATE_STATUS_VIEW_EVENT, info);
            uiHandler.sendMessage(msg);
        }
    }

    public static void main(String[] args) {
        class TestProvider implements SensorInfoProvider {
            private int value;
            public SensorInfo getSensorInfo() {
                SensorInfo sensorInfo = new SensorInfo(value, value, value, value, value, value);
                value++;
                return sensorInfo;
            }
        }
        int port = 8080;
        MultiplexerServer server = new MultiplexerServer(port, new TestProvider(), null);
        new Thread(server).start();
    }

    /*private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            //new connection
            if (key.isAcceptable()) {
                ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
                SocketChannel sc = ssc.accept();
                sc.configureBlocking(false);
                sc.register(selector, SelectionKey.OP_READ);
            }
            if (key.isReadable()) {
                SocketChannel sc = (SocketChannel)key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    //String body = new String(bytes, "UTF-8");
                    System.out.println("the server receive :" + bytes);
                } else if (readBytes < 0) {
                    key.cancel();
                    sc.close();
                } else {
                    System.out.println("read byte is zero");
                }
            }
        }
    }

    private void doWrite(SocketChannel channel, String response) throws IOException {
        if (response != null && response.trim().length() > 0) {
            byte[] bytes = response.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();
            channel.write(writeBuffer);
        }
    }*/
}
