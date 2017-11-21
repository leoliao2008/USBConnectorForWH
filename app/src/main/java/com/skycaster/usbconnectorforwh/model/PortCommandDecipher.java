package com.skycaster.usbconnectorforwh.model;

import android.util.Log;

import com.skycaster.usbconnectorforwh.data.StaticData;
import com.skycaster.usbconnectorforwh.utils.NumberFormatter;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by 廖华凯 on 2017/11/14.
 */

public class PortCommandDecipher {

    private boolean isRequestConfirm;
    private int ffCount;
    private int index;
    private PortCommandDecipher.CallBack mCallBack;
    private byte[] temp;
    private ThreadPoolExecutor mThreadPoolExecutor;
    private Runnable mRunnableDecipher;


    /*************************************开始运行****************************************/

    private PortCommandDecipher() {}

    public PortCommandDecipher(CallBack callBack) {
        mCallBack=callBack;
        temp=new byte[StaticData.VALID_DATA_LENGTH];
        mThreadPoolExecutor=new ThreadPoolExecutor(
                1,
                1,
                30,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>()
        );
        mRunnableDecipher=new Runnable() {
            @Override
            public void run() {
                decipher(temp.clone());
            }
        };
    }

    public void decipher(byte[] input, int len){
        for (int i=0;i<len;i++){
//            String format = String.format(Locale.CHINA, "%02x", input[i]);
//            showLog("0x"+format);
            //先确定指令开始的信号
            if(!isRequestConfirm){
//                showLog("request confirming...");
                if(input[i]==((byte) 0xff)){
                    ffCount++;
                }else {
                    ffCount=0;
                }
                //连续5个0xff，基本可以确定是收到串口发过来的指令了。
                if(ffCount==5){
                    ffCount=0;
                    isRequestConfirm=true;
                    index=0;
//                    showLog("confirm request.");
                }
            }else {
                //串口协议的指令起始位置有10个0xff，在这里把其他的0xff忽略掉
                if(index==0&&input[i]==((byte) 0xff)){
                    continue;
                }
                //把指令核心部分的5个字节依次转到临时容器里
                temp[index]=input[i];
                //5个字节过后，重新回到最初的接收状态
                if(index==4){
                    isRequestConfirm=false;
                    ffCount=0;
                    //把5个核心字节扔到线程池中解析，回头接收其他指令。
                    mThreadPoolExecutor.execute(mRunnableDecipher);
//                    decipher(temp.clone());
                }else {
                    index++;
                }
            }
        }
    }

    private void decipher(byte[] dest) {
//        showLog("begin to decipher");
        switch (dest[0]){
            case 0x0A://设置DSP参数
                int i =0xff&dest[1];//主频小数点左边的数值
                int j=0xff&dest[2];//主频小数点右边的数值
                int leftTune=0xff&dest[3];
                int rightTune=0xff&dest[4];
                try {
                    double freq = NumberFormatter.getDouble(i,j,2);
//                    showLog("freq "+freq+" left "+leftTune+" right "+rightTune);
                    mCallBack.onRequestResetDspParams(freq,leftTune,rightTune);
                }catch (NumberFormatException e){
                    mCallBack.onInvalidDspParamsInput(e.getMessage());
                }
                break;
            default:
                break;
        }
    }

    public boolean clearTasks(){
        return mThreadPoolExecutor.remove(mRunnableDecipher);
    }

    public interface CallBack{
        void onRequestResetDspParams(double freq, int leftTune, int rightTune);

        void onInvalidDspParamsInput(String msg);
    }

    private void showLog(String msg){
        Log.e(getClass().getSimpleName(), msg);
    }
}
