package com.skycaster.usbconnectorforwh.activity;

import android.view.View;
import android.widget.ListView;

import com.skycaster.usbconnectorforwh.R;
import com.skycaster.usbconnectorforwh.base.MyCDRadioActivity;
import com.skycaster.usbconnectorforwh.presenter.MainActivityPresenter;

public class MainActivity extends MyCDRadioActivity {

    private MainActivityPresenter mPresenter;
    private ListView mListView;

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        mListView=findViewById(R.id.main_list_view);

    }

    public ListView getListView() {
        return mListView;
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

    public void openDsp(View view) {
        mPresenter.openDspWithPreviousParams();
    }

    public void startBizService(View view) {
        mPresenter.startBizDataService();
    }
    public void stopBizService(View view) {
        mPresenter.stopBizDataService();
    }

    public void closeDsp(View view) {
        mPresenter.closeDsp();
    }


    public void resetDsp(View view) {
        mPresenter.displayResetDspDialog();
    }
}
