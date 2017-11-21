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
import com.skycaster.usbconnectorforwh.utils.AlertDialogUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

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
            showLog("DSP Connection established!");
        }

        @Override
        public void onDspDisconnect() {
            showLog("DSP Disconnected!");
        }
    };
    private SharedPreferences mSharedPreferences;
    private BusinessDataListener mBusinessDataListener=new BusinessDataListener() {
        @Override
        public void preTask() {
            showLog("Initializing Biz Data Transferring...");
        }

        @Override
        public void onGetBizData(final byte[] bytes) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    StringBuffer sb=new StringBuffer();
                    sb.append("Biz Data:");
                    for(byte temp:bytes){
                        sb.append("0x");
                        sb.append(String.format(Locale.CHINA,"%02X",temp));
                        sb.append(" ");
                    }
                    showLog(sb.toString().trim());
                }
            });
        }

        @Override
        public void onServiceStop() {
            showLog("Biz Data Transferring Terminated.");
        }
    };
    private PortCommandDecipher mDecipher;
    private PortCommandDecipher.CallBack mDecipherCallBack =new PortCommandDecipher.CallBack() {
        @Override
        public void onRequestResetDspParams(final double freq, final int leftTune, final int rightTune) {
            openDspWithNewParams(freq,leftTune,rightTune);
        }

        @Override
        public void onInvalidDspParamsInput(String msg) {
            showLog("Reset Dsp Params fails ："+msg);
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
        mDecipher =new PortCommandDecipher(mDecipherCallBack);
        mSharedPreferences=mActivity.getSharedPreferences(StaticData.SP_NAME, Context.MODE_PRIVATE);

        //初始化主页面list view
        mAdapter=new ArrayAdapter<String>(
                mActivity,
                android.R.layout.simple_list_item_1,
                mLogs
        );
        mActivity.getListView().setAdapter(mAdapter);

        //打开串口到用户电脑端
        try {
            mSerialPort = mSerialPortModel.openSerialPort(StaticData.SERIAL_PORT_PATH, StaticData.SERIAL_PORT_BAUD_RATE);
            mInputStream= mSerialPort.getInputStream();
            mOutputStream=mSerialPort.getOutputStream();
            startReceivingPortData();
        } catch (Exception e) {
            handleException(e);
        }

        //启动业务数据传输
        openDspWithPreviousParams();
    }


    public void openDspWithPreviousParams(){
        try {
            Double freq = Double.valueOf(mSharedPreferences.getString(StaticData.DSP_FREQ, StaticData.DEFAULT_FREQ_VALUE));
            int leftTune = mSharedPreferences.getInt(StaticData.DSP_LEFT_TUNE, StaticData.DEFAULT_LEFT_TUNE_VALUE);
            int rightTune = mSharedPreferences.getInt(StaticData.DSP_RIGHT_TUNE, StaticData.DEFAULT_RIGHT_TUNE_VALUE);
            boolean isDspOpen = mDspModel.openDsp(
                    freq,
                    leftTune,
                    rightTune
            );
            if(isDspOpen){
                showLog("Dsp Open Success! Freq is "+freq+" left tune is "+leftTune+" right tune is "+rightTune);
            }else {
                showLog("Fail to Open Dsp.");
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDspModel.startReceivingData(mActivity,mBusinessDataListener);
                }
            },1000);
        } catch (DSPManager.FreqOutOfRangeException e) {
            handleException(e);
        }

    }

    private void openDspWithNewParams(final double freq, final int leftTune, final int rightTune){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(StaticData.DSP_FREQ,String.valueOf(freq));
        editor.putInt(StaticData.DSP_LEFT_TUNE,leftTune);
        editor.putInt(StaticData.DSP_RIGHT_TUNE,rightTune);
        editor.apply();
        showLog("new freq is "+freq+" new left tune is "+leftTune+" new right tune is "+rightTune+" , please restart your device!");
//        mDspModel.resetDsp(freq, leftTune, rightTune, mActivity, mBusinessDataListener, new DspModel.CallBack() {
//            @Override
//            public void onProcessing(String info) {
//                showLog(info);
//            }
//
//            @Override
//            public void onSetParamsSuccess(String msg) {
//                showLog("Reset Dsp Params Success,new freq is "+freq+" new left tune is "+leftTune+" new right tune is "+rightTune);
//                SharedPreferences.Editor editor = mSharedPreferences.edit();
//                editor.putString(StaticData.DSP_FREQ,String.valueOf(freq));
//                editor.putInt(StaticData.DSP_LEFT_TUNE,leftTune);
//                editor.putInt(StaticData.DSP_RIGHT_TUNE,rightTune);
//                editor.apply();
//            }
//
//            @Override
//            public void onRestartingBizServiceSuccess() {
//                showLog("Restarting Biz Data Transferring Success! New freq is "+freq+" new left tune is "+leftTune+" new right tune is "+rightTune);
//            }
//
//            @Override
//            public void onRestartBizServiceFails(String msg) {
//                showLog("Restart Biz Data Transferring Fails. Try to restart the device, Perhaps?");
//            }
//
//            @Override
//            public void onResetFails(String msg) {
//                showLog("Reset Dsp Params fails ："+msg);
//            }
//        });
    }



    public void displayResetDspDialog() {
        AlertDialogUtil.showResetDspParamsDialog(
                mActivity,
                Double.valueOf(mSharedPreferences.getString(StaticData.DSP_FREQ, StaticData.DEFAULT_FREQ_VALUE)),
                mSharedPreferences.getInt(StaticData.DSP_LEFT_TUNE, StaticData.DEFAULT_LEFT_TUNE_VALUE),
                mSharedPreferences.getInt(StaticData.DSP_RIGHT_TUNE, StaticData.DEFAULT_RIGHT_TUNE_VALUE),
                new AlertDialogUtil.InputDspParamListener() {
                    @Override
                    public void onInputDspParamsConfirm(final Double freq, final Integer leftTune, final Integer rightTune) {
                        openDspWithNewParams(freq,leftTune,rightTune);
                    }
                }
        );
    }

    public boolean closeDsp(){
        return mDspModel.closeDsp();
    }

    public boolean startBizDataService(){
        return mDspModel.startReceivingData(mActivity,mBusinessDataListener);
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
                            mDecipher.decipher(temp,len);
                        }
                    } catch (Exception e) {
                        handleException(e);
                        break;
                    }
                }
            }
        });
        mThread.setPriority(Thread.MAX_PRIORITY);
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
        IntentFilter filter=new IntentFilter(StaticData.ACTION_DETECT_DSP_STATUS);
        mReceiver = new DspConnectStatusListener(mDspConnectStatusCallBack);
        mActivity.registerReceiver(mReceiver,filter);
    }

    public void onStop() {
        unregisterReceivers();
        if(mActivity.isFinishing()){
            stopBizDataService();
            closeDsp();
            stopReceivingPortData();
            mDecipher.clearTasks();
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
                        mOutputStream.write("\r\n".getBytes());
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

    public boolean stopBizDataService() {
        return mDspModel.stopReceivingData();
    }
}
