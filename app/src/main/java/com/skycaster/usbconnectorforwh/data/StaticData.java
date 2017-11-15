package com.skycaster.usbconnectorforwh.data;

/**
 * Created by 廖华凯 on 2017/11/14.
 */

public interface StaticData {
    String IS_DSP_CONNECTED="IS_DSP_CONNECTED";
    String ACTION_DECTECT_DSP_STATUS="com.skycaster.action.DSP_CONNECTION_CHANGES";
    String SP_NAME = "Config";
    String DSP_FREQ = "DSP_FREQ";
    String DSP_LEFT_TUNE="DSP_LEFT_TUNE";
    String DSP_RIGHT_TUNE="DSP_RIGHT_TUNE";
    int VALID_DATA_LENGTH = 5;
    String SERIAL_PORT_PATH="/dev/ttyS1";
    int SERIAL_PORT_BAUD_RATE =115200;
    String DEFAULT_FREQ_VALUE = "98";
    int DEFAULT_LEFT_TUNE_VALUE=36;
    int DEFAULT_RIGHT_TUNE_VALUE=45;

}
