package com.skycaster.usbconnectorforwh.model;

import com.skycaster.usbconnectorforwh.utils.NumberFormatter;
import com.skycaster.usbconnectorforwh.data.StaticData;

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

    public void extractValidData(byte[] input, int len){
        for (int i=0;i<len;i++){
            //先确定指令开始的信号
            if(!isRequestConfirm){
                if(input[i]==0xff){
                    ffCount++;
                }else {
                    ffCount=0;
                }
                //连续5个0xff，基本可以确定是收到串口发过来的指令了。
                if(ffCount==5){
                    ffCount=0;
                    isRequestConfirm=true;
                    index=0;
                }
            }else {
                //串口协议的指令起始位置有10个0xff，在这里把其他的0xff忽略掉
                if(index==0&&input[i]==0xff){
                    continue;
                }
                //把指令核心部分的5个字节依次转到临时容器里
                temp[index]=input[i];
                index++;
                //5个字节过后，重新回到最初的接收状态
                if(index==5){
                    isRequestConfirm=false;
                    ffCount=0;
                    //把5个核心字节扔到线程池中解析，回头接收其他指令。
                    mThreadPoolExecutor.execute(mRunnableDecipher);
                }
            }
        }
    }

    private void decipher(byte[] dest) {

        switch (dest[0]){
            case 0x0A://设置DSP参数
                int i =0xff&dest[1];//主频小数点左边的数值
                int j=0xff&dest[2];//主频小数点右边的数值
                int leftTune=0xff&dest[3];
                int rightTune=0xff&dest[4];
                try {
                    double freq = NumberFormatter.getDouble(i,j,2);
                    mCallBack.onRequestResetParams(freq,leftTune,rightTune);
                }catch (NumberFormatException e){
                    mCallBack.onDspParamsInvalid(e.getMessage());
                }
                break;
            default:
                break;
        }
    }

    public interface CallBack{
        void onRequestResetParams(double freq,int leftTune,int rightTune);

        void onDspParamsInvalid(String msg);
    }
}
