package com.skycaster.usbconnectorforwh.model;

/**
 * Created by 廖华凯 on 2017/11/22.
 */

public class DurationTestModel {
    boolean isFrameHeadConfirm=false;
    private byte[] content=new byte[4];
    private int index;
    private DurationTestModel.CallBack mCallBack;
    private long mLastTime;
    private long mThisTime;

    private DurationTestModel(){}

    public DurationTestModel(DurationTestModel.CallBack callBack) {
        mLastTime=System.currentTimeMillis();
        mCallBack = callBack;
    }

    public void decypher(byte[] data){
        for (int i=0,len=data.length;i<len;i++){
            byte temp=data[i];
            if(!isFrameHeadConfirm){
                if(temp==(byte)(0xB7)){
                    isFrameHeadConfirm=true;
                    index=0;
                }
            }else {
                content[index++]=temp;
                if(index==4){
                    isFrameHeadConfirm=false;
                    index=0;
                    int i1=(content[0]<<24)&0xffffffff;
                    int i2=(content[1]<<16)&0xffffffff;
                    int i3=(content[2]<<8)&0xffffffff;
                    int i4=content[3]&0xff;
                    mThisTime=System.currentTimeMillis();
                    long duration=mThisTime-mLastTime;
                    mLastTime=mThisTime;
                    mCallBack.onGetFrameId(i1+i2+i3+i4,duration);
                }
            }
        }
    }

    public interface CallBack{
        void onGetFrameId(int result, long duration);
    }
}
