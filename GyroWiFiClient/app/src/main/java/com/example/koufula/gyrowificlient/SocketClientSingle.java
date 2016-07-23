package com.example.koufula.gyrowificlient;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by oxiao on 2016/7/20.
 */
public class SocketClientSingle {
    private static Socket s = null;
    private SocketClientSingle() {

    }
    public static Socket getSocket(String ip,int port){
        try {
            s = new Socket(ip,port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }
}
