package com.skycaster.usbconnectorforwh.base;

import android.os.Bundle;

import com.skycaster.skc_cdradiorx.bases.CDRadioActivity;

/**
 * Created by 廖华凯 on 2017/11/14.
 */

public abstract class MyCDRadioActivity extends CDRadioActivity {

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(getContentView());
        initView();
        initData();
        initListener();
    }

    protected abstract int getContentView();

    protected abstract void initView();

    protected abstract void initData();

    protected abstract void initListener();





}
