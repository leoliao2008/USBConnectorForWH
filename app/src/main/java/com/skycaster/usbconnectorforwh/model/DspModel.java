package com.skycaster.usbconnectorforwh.model;

import android.app.Activity;

import com.skycaster.skc_cdradiorx.abstr.BusinessDataListener;
import com.skycaster.skc_cdradiorx.manager.DSPManager;

/**
 * Created by 廖华凯 on 2017/11/14.
 */

public class DspModel {
    private DSPManager mDSPManager;

    public DspModel() {
        mDSPManager=DSPManager.getDSPManager();
    }

    public boolean openDsp(double freq,int leftTune,int rightTune) throws DSPManager.FreqOutOfRangeException {
        return mDSPManager.apiOpenCDRadio(freq,leftTune,rightTune);
    }

    public boolean closeDsp(){
        return mDSPManager.apiStopDevice();
    }

    public boolean startReceivingData(Activity context, BusinessDataListener listener){
        return mDSPManager.apiGetService(context,91, (byte) 33,listener);
    }

    public boolean stopReceivingData(){
        return mDSPManager.apiStopService();
    }

    public void resetDsp(
            final double freq,
            final int leftTune,
            final int rightTune,
            final Activity context,
            final BusinessDataListener listener,
            final DspModel.CallBack callBack)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {

                if(stopReceivingData()){
                    callBack.onProcessing("Terminate Biz Data Transferring Success.");
                }else {
                    callBack.onProcessing("Terminate Biz Data Transferring Fails.");
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(closeDsp()){
                    callBack.onProcessing("Close Dsp Successfully.");
                }else {
                    callBack.onProcessing("Unable to Close Dsp.");
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    if(openDsp(freq,leftTune,rightTune)){
                        callBack.onSetParamsSuccess("Reset Dsp with New Params:Success! Trying to restart Biz Data Transferring...");
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        boolean isSuccess = startReceivingData(context, listener);
                        callBack.onProcessing("Restarting Dsp Biz Data Transferring...");
                        if(isSuccess){
                            callBack.onRestartingBizServiceSuccess();
                        }else {
                            callBack.onRestartBizServiceFails("Cannot restart Dsp Biz Data Transferring.");
                        }
                    }else {
                        callBack.onResetFails("Open Dsp Fails ：Freq："+freq+" Left tune："+leftTune+" Right Tune："+rightTune);
                    }
                } catch (DSPManager.FreqOutOfRangeException e) {
                    callBack.onResetFails(e.getMessage());
                }
            }
        }).start();
    }

    public interface CallBack{
        void onProcessing(String info);
        void onSetParamsSuccess(String msg);
        void onRestartBizServiceFails(String msg);
        void onResetFails(String msg);
        void onRestartingBizServiceSuccess();

    }
}
