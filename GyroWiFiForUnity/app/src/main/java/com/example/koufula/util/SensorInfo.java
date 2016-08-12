package com.example.koufula.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by ezhalel on 2016/7/21.
 */
public class SensorInfo implements Serializable {

    private float mGyroX;
    private float mGyroY;
    private float mGyroZ;

    private float mAccX;
    private float mAccY;
    private float mAccZ;
    private static final long serialVersionUID =1L;

    public SensorInfo() {

    }

    public SensorInfo(float gyrox, float gyroy, float gyroz, float accx, float accy, float accz){
        mGyroX = gyrox;
        mGyroY = gyroy;
        mGyroZ = gyroz;

        mAccX = accx;
        mAccY = accy;
        mAccZ = accz;
    }

    public float getmGyroX() {
        return mGyroX;
    }

    public void setmGyroX(float mGyroX) {
        this.mGyroX = mGyroX;
    }

    public float getmGyroY() {
        return mGyroY;
    }

    public void setmGyroY(float mGyroY) {
        this.mGyroY = mGyroY;
    }

    public float getmGyroZ() {
        return mGyroZ;
    }

    public void setmGyroZ(float mGyroZ) {
        this.mGyroZ = mGyroZ;
    }

    public float getmAccX() {
        return mAccX;
    }

    public void setmAccX(float mAccX) {
        this.mAccX = mAccX;
    }

    public float getmAccY() {
        return mAccY;
    }

    public void setmAccY(float mAccY) {
        this.mAccY = mAccY;
    }

    public float getmAccZ() {
        return mAccZ;
    }

    public void setmAccZ(float mAccZ) {
        this.mAccZ = mAccZ;
    }

    public String toString() {
        return "mGyroX:" + mGyroX + ", mGyroY:" + mGyroY + ", mGyroZ:" + mGyroZ +
                ", mAccX:" + mAccZ + ", mAccY:" + mAccY + ", mAccZ:" + mAccZ + "\n\r";
    }

}
