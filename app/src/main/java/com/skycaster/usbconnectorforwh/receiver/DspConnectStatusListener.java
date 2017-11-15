package com.skycaster.usbconnectorforwh.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.skycaster.usbconnectorforwh.data.StaticData;

/**
 * Created by 廖华凯 on 2017/11/14.
 */

public class DspConnectStatusListener extends BroadcastReceiver {
    private DspConnectStatusListener.CallBack mCallBack;

    public DspConnectStatusListener(CallBack callBack) {
        mCallBack = callBack;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean flag = intent.getBooleanExtra(StaticData.IS_DSP_CONNECTED, false);
        if(flag){
            mCallBack.onDspConnect();
        }else {
            mCallBack.onDspDisconnect();
        }
    }

    public interface CallBack{
        void onDspConnect();
        void onDspDisconnect();
    }
}
