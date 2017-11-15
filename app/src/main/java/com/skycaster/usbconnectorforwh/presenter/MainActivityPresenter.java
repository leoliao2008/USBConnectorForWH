package com.skycaster.usbconnectorforwh.presenter;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.skycaster.skc_cdradiorx.abstr.BusinessDataListener;
import com.skycaster.skc_cdradiorx.manager.DSPManager;
import com.skycaster.usbconnectorforwh.activity.MainActivity;
import com.skycaster.usbconnectorforwh.data.StaticData;
import com.skycaster.usbconnectorforwh.model.DspModel;
import com.skycaster.usbconnectorforwh.model.PortCommandDecipher;
import com.skycaster.usbconnectorforwh.model.SerialPortModel;
import com.skycaster.usbconnectorforwh.receiver.DspConnectStatusListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import project.SerialPort.SerialPort;

/**
 * Created by 廖华凯 on 2017/11/14.
 */

public class MainActivityPresenter {
    private MainActivity mActivity;
    private DspConnectStatusListener mReceiver;
    private DspModel mDspModel;
    private SerialPortModel mSerialPortModel;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private SerialPort mSerialPort;
    private Handler mHandler;
    private Thread mThread;
    private byte[] temp=new byte[256];
    private DspConnectStatusListener.CallBack mDspConnectStatusCallBack =new DspConnectStatusListener.CallBack() {
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
    private PortCommandDecipher mCommandDecipher;
    private PortCommandDecipher.CallBack mDecipherCallBack =new PortCommandDecipher.CallBack() {
        @Override
        public void onRequestResetParams(final double freq, final int leftTune, final int rightTune) {

            mDspModel.resetDsp(freq, leftTune, rightTune, mActivity, mBusinessDataListener, new DspModel.CallBack() {
                @Override
                public void onResetComplete() {
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString(StaticData.DSP_FREQ,String.valueOf(freq));
                    editor.putInt(StaticData.DSP_LEFT_TUNE,leftTune);
                    editor.putInt(StaticData.DSP_RIGHT_TUNE,rightTune);
                    editor.apply();
                    showLog("参数设置成功，新的主频："+freq+" 新的左频："+leftTune+" 新的右频："+rightTune);
                }

                @Override
                public void onResetFails(String msg) {
                    showLog("参数设置失败，原因："+msg);
                }
            });
        }

        @Override
        public void onDspParamsInvalid(String msg) {
            showLog("参数设置失败，原因："+msg);
        }
    };
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mLogs=new ArrayList<>();




/*******************************************************开始运行********************************************************/

    public MainActivityPresenter(MainActivity activity) {
        mActivity = activity;
    }

    public void init(){
        mDspModel=new DspModel();
        mSerialPortModel=new SerialPortModel();
        mHandler=new Handler(mActivity.getMainLooper());
        mCommandDecipher=new PortCommandDecipher(mDecipherCallBack);

        //初始化主页面list view
        mAdapter=new ArrayAdapter<String>(
                mActivity,
                android.R.layout.simple_list_item_1,
                mLogs
        );
        mActivity.getListView().setAdapter(mAdapter);

        //读取上次的启动参数
        mSharedPreferences=mActivity.getSharedPreferences(StaticData.SP_NAME, Context.MODE_PRIVATE);
        double freq = Double.valueOf(mSharedPreferences.getString(StaticData.DSP_FREQ, StaticData.DEFAULT_FREQ_VALUE));
        int leftTune = mSharedPreferences.getInt(StaticData.DSP_LEFT_TUNE, StaticData.DEFAULT_LEFT_TUNE_VALUE);
        int rightTune = mSharedPreferences.getInt(StaticData.DSP_RIGHT_TUNE, StaticData.DEFAULT_RIGHT_TUNE_VALUE);

        //利用上次的参数启动dsp
        try {
            mDspModel.openDsp(freq, leftTune, rightTune);
            mDspModel.startReceivingData(mActivity,mBusinessDataListener);
        } catch (DSPManager.FreqOutOfRangeException e) {
            handleException(e);
        }

        //打开串口到用户电脑端
        try {
            mSerialPort = mSerialPortModel.openSerialPort(StaticData.SERIAL_PORT_PATH, StaticData.SERIAL_PORT_BAUD_RATE);
            mInputStream= mSerialPort.getInputStream();
            mOutputStream=mSerialPort.getOutputStream();
            startReceivingPortData();
        } catch (Exception e) {
            handleException(e);
        }
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
                            mCommandDecipher.extractValidData(temp,len);
                        }
                    } catch (Exception e) {
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
        mReceiver = new DspConnectStatusListener(mDspConnectStatusCallBack);
        mActivity.registerReceiver(mReceiver,filter);
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
        updateListView(msg);
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

    private void updateListView(final String msg){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int size = mLogs.size();
                if(size>=50){
                    mLogs.remove(0);
                }
                mLogs.add(msg);
                mAdapter.notifyDataSetChanged();
                mActivity.getListView().smoothScrollToPosition(Integer.MAX_VALUE);
            }
        });
    }
}
