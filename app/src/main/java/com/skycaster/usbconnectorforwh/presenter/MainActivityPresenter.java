package com.skycaster.usbconnectorforwh.presenter;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.skycaster.skc_cdradiorx.abstr.BusinessDataListener;
import com.skycaster.skc_cdradiorx.manager.DSPManager;
import com.skycaster.usbconnectorforwh.activity.MainActivity;
import com.skycaster.usbconnectorforwh.data.StaticData;
import com.skycaster.usbconnectorforwh.model.DspModel;
import com.skycaster.usbconnectorforwh.model.SerialPortModel;
import com.skycaster.usbconnectorforwh.receiver.DspConnectStateListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import project.SerialPort.SerialPort;

/**
 * Created by 廖华凯 on 2017/11/14.
 */

public class MainActivityPresenter {
    private MainActivity mActivity;
    private DspConnectStateListener mReceiver;
    private DspModel mDspModel;
    private SerialPortModel mSerialPortModel;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private SerialPort mSerialPort;
    private Handler mHandler;
    private Thread mThread;
    private byte[] temp=new byte[256];
    private DspConnectStateListener.CallBack mCallBack=new DspConnectStateListener.CallBack() {
        @Override
        public void onDspConnect() {
            showLog("DSP连接成功！");
        }

        @Override
        public void onDspDisconnect() {
            showLog("DSP连接中断了！");
        }
    };
    private SharedPreferences mSharedPreferences;
    private double mFreq;
    private int mLeftTune;
    private int mRightTune;
    private BusinessDataListener mBusinessDataListener=new BusinessDataListener() {
        @Override
        public void preTask() {
            showLog("业务数据正在启动...");
        }

        @Override
        public void onGetBizData(byte[] bytes) {
            showLog(new String(bytes));
        }

        @Override
        public void onServiceStop() {
            showLog("业务数据停止了。");
        }
    };






    public MainActivityPresenter(MainActivity activity) {
        mActivity = activity;
    }

    public void init(){
        mDspModel=new DspModel();
        mSerialPortModel=new SerialPortModel();
        mHandler=new Handler(mActivity.getMainLooper());

        mSharedPreferences=mActivity.getSharedPreferences(StaticData.SP_NAME, Context.MODE_PRIVATE);
        mFreq=Double.valueOf(mSharedPreferences.getString(StaticData.DSP_FREQ,"98"));
        mLeftTune=mSharedPreferences.getInt(StaticData.DSP_LEFT_TUNE,36);
        mRightTune=mSharedPreferences.getInt(StaticData.DSP_RIGHT_TUNE,45);

        try {
            mDspModel.openDsp(mFreq,mLeftTune,mRightTune);
            mDspModel.startReceivingData(mActivity,getBizDataListener());
        } catch (DSPManager.FreqOutOfRangeException e) {
            handleException(e);
        }

        try {
            mSerialPort = mSerialPortModel.openSerialPort("/dev/ttyS0", 115200);
            mInputStream= mSerialPort.getInputStream();
            mOutputStream=mSerialPort.getOutputStream();
            startReceivingPortData();
        } catch (Exception e) {
            handleException(e);
        }
    }

    private BusinessDataListener getBizDataListener() {
        return mBusinessDataListener;
    }

    private void startReceivingPortData() {

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        if(mInputStream.available()>0){
                            if(mThread.isInterrupted()){
                                break;
                            }
                            int len = mInputStream.read(temp);
                            //// TODO: 2017/11/14 按照协议解析
                        }
                    } catch (IOException e) {
                        handleException(e);
                        break;
                    }
                }
            }
        });
        mThread.start();
    }

    private void stopReceivingPortData(){
        if(mThread!=null){
            mThread.interrupt();
            mThread=null;
        }
    }

    public void onStart() {
        registerReceivers();
    }

    private void registerReceivers() {
        IntentFilter filter=new IntentFilter(StaticData.ACTION_DECTECT_DSP_STATUS);
        mReceiver = new DspConnectStateListener(getDspStatusListener());
        mActivity.registerReceiver(mReceiver,filter);
    }

    private DspConnectStateListener.CallBack getDspStatusListener() {
        return mCallBack;
    }


    public void onStop() {
        unregisterReceivers();
        if(mActivity.isFinishing()){
            stopReceivingPortData();
            if(mSerialPort!=null){
                mSerialPortModel.closeSerialPort(mSerialPort);
                mSerialPort=null;
            }
        }
    }

    private void unregisterReceivers() {
        if(mReceiver!=null){
            mActivity.unregisterReceiver(mReceiver);
            mReceiver=null;
        }
    }

    private void handleException(Exception e){
        String msg = e.getMessage();
        if(TextUtils.isEmpty(msg)){
            msg="Exception Unknown.";
        }
        showLog(msg);
    }

    private void showLog(String msg){
        Log.e(getClass().getSimpleName(),msg);
        sendMessageToSerialPort(msg.getBytes());
    }

    private void sendMessageToSerialPort(final byte[] msg){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mOutputStream!=null){
                    try {
                        mOutputStream.write(msg);
                        mOutputStream.flush();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }
}
