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
                    callBack.onProcessing("已关闭裸数据传输");
                }else {
                    callBack.onProcessing("关闭裸数据传输失败");
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(closeDsp()){
                    callBack.onProcessing("已关闭DSP");
                }else {
                    callBack.onProcessing("DSP关闭失败");
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    if(openDsp(freq,leftTune,rightTune)){
                        callBack.onProcessing("用新配置参数重启DSP成功");
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        boolean isSuccess = startReceivingData(context, listener);
                        callBack.onProcessing("重新启动业务数据传输...");
                        if(isSuccess){
                            callBack.onResetComplete();
                        }else {
                            callBack.onResetFails("无法启动业务传输，请检查DSP连接。");
                        }
                    }else {
                        callBack.onResetFails("DSP启动失败 配置参数：主频："+freq+" 左频："+leftTune+" 右频："+rightTune);
                    }
                } catch (DSPManager.FreqOutOfRangeException e) {
                    callBack.onResetFails(e.getMessage());
                }
            }
        }).start();
    }

    public interface CallBack{
        void onProcessing(String info);
        void onResetComplete();
        void onResetFails(String msg);
    }
}
