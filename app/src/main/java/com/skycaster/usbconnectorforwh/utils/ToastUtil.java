package com.skycaster.usbconnectorforwh.utils;

import android.widget.Toast;

import com.skycaster.skc_cdradiorx.bases.CDRadioApplication;

/**
 * Created by 廖华凯 on 2017/11/17.
 */

public class ToastUtil {
    private static Toast toast;
    public static void showToast(String msg){
        if(toast==null){
            toast=Toast.makeText(CDRadioApplication.getGlobalContext(),msg,Toast.LENGTH_SHORT);
        }else {
            toast.setText(msg);
        }
        toast.show();
    }
}
