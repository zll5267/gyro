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

/**
 * Created by ezhalel on 2016/8/23.
 */
public class MultiplexerServer implements Runnable{

    private Selector selector;
    private ServerSocketChannel servChannel;
    private volatile boolean stop;
    private boolean connected;// now we only support one client
    private SocketChannel connectChannel;//the connection with the client

    SensorInfoProvider sensorInfoProvider;

    public MultiplexerServer(int port, SensorInfoProvider provider) {
        try {
            selector = Selector.open();
            servChannel = ServerSocketChannel.open();
            servChannel.configureBlocking(false);
            servChannel.socket().bind(new InetSocketAddress(port), 10);
            servChannel.register(selector, SelectionKey.OP_ACCEPT);
            sensorInfoProvider = provider;
            System.out.println("the server is start on port:" + port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void stop() {
        this.stop = true;
    }

    @Override
    public void run() {
        connected = false;
        while(!stop) {
            try {
                if (!connected) {
                    selector.select(1000);
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> it = selectedKeys.iterator();
                    SelectionKey key = null;
                    while (it.hasNext()) {
                        key = it.next();
                        it.remove();
                        try {
                            //handleInput(key);
                            if (key.isValid()) {
                                //new connection
                                if (key.isAcceptable()) {
                                    ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                                    connectChannel = ssc.accept();
                                    connectChannel.configureBlocking(false);
                                    connected = true;
                                    //connectChannel.register(selector, SelectionKey.OP_READ);
                                }
                            }
                        } catch (Exception e) {
                            if (key != null) {
                                key.cancel();
                                if (key.channel() != null)
                                    key.channel().close();
                                ;
                            }
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
                    Thread.sleep(200);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();;
            }
        }
    }

    public static void main(String[] args) {
        class TestProvider implements SensorInfoProvider {
            public SensorInfo getSensorInfo() {
                SensorInfo sensorInfo = new SensorInfo(1, 2, 3, 4, 5, 6);
                return sensorInfo;
            }
        }
        int port = 8080;
        MultiplexerServer server = new MultiplexerServer(port, new TestProvider());
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
