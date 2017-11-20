package com.skycaster.usbconnectorforwh.utils;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.skycaster.usbconnectorforwh.R;

/**
 * Created by 廖华凯 on 2017/11/17.
 */

public class AlertDialogUtil {

    private static AlertDialog alertDialog;

    public static void showResetDspParamsDialog(
            Context context,
            double defaultFrq,
            int defaultLeftTune,
            int defaultRightTune,
            final InputDspParamListener listener){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        //init view
        View rootView=View.inflate(context, R.layout.alert_dialog_open_dsp,null);
        final EditText edt_freq=rootView.findViewById(R.id.alert_dialog_open_dsp_edt_frq);
        final EditText edt_leftTune=rootView.findViewById(R.id.alert_dialog_open_dsp_edt_left_tune);
        final EditText edt_rightTune=rootView.findViewById(R.id.alert_dialog_open_dsp_edt_right_tune);
        Button btn_confirm=rootView.findViewById(R.id.alert_dialog_open_dsp_btn_confirm);
        Button btn_cancel=rootView.findViewById(R.id.alert_dialog_open_dsp_btn_cancel);
        //init data
        assignValueToEditText(String.valueOf(defaultFrq),edt_freq);
        assignValueToEditText(String.valueOf(defaultLeftTune),edt_leftTune);
        assignValueToEditText(String.valueOf(defaultRightTune),edt_rightTune);
        //init listeners
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_freq=edt_freq.getText().toString().trim();
                if(TextUtils.isEmpty(str_freq)){
                    ToastUtil.showToast("请输入主频！");
                    return;
                }
                String str_leftTune = edt_leftTune.getText().toString().trim();
                if(TextUtils.isEmpty(str_leftTune)){
                    ToastUtil.showToast("请输入左频！");
                    return;
                }
                String str_rightTune=edt_rightTune.getText().toString().trim();
                if(TextUtils.isEmpty(str_rightTune)){
                    ToastUtil.showToast("请输入右频！");
                    return;
                }
                listener.onInputDspParamsConfirm(
                        Double.valueOf(str_freq),
                        Integer.valueOf(str_leftTune),
                        Integer.valueOf(str_rightTune)
                );
                alertDialog.dismiss();

            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog = builder.setView(rootView).setCancelable(false).create();
        alertDialog.show();
    }

    private static void assignValueToEditText(String value,EditText editText){
        editText.setText(value);
        editText.setSelection(value.length());
    }

    public interface InputDspParamListener{
        void onInputDspParamsConfirm(Double freq, Integer leftTune, Integer rightTune);
    }
}
