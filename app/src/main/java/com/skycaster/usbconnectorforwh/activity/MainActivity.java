package com.skycaster.usbconnectorforwh.activity;

import com.skycaster.usbconnectorforwh.R;
import com.skycaster.usbconnectorforwh.base.MyCDRadioActivity;
import com.skycaster.usbconnectorforwh.presenter.MainActivityPresenter;

public class MainActivity extends MyCDRadioActivity {

    private MainActivityPresenter mPresenter;

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        mPresenter=new MainActivityPresenter(this);
        mPresenter.init();

    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.onStop();
    }
}
