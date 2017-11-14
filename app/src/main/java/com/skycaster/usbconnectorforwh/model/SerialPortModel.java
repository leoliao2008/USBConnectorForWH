package com.skycaster.usbconnectorforwh.model;

import java.io.File;
import java.io.IOException;

import project.SerialPort.SerialPort;
import project.SerialPort.SerialPortFinder;

/**
 * Created by 廖华凯 on 2017/11/14.
 */

public class SerialPortModel {

    public String[] getSerialPortPaths(){
        return new SerialPortFinder().getAllDevicesPath();
    }

    public SerialPort openSerialPort(String path,int baudRate) throws IOException,SecurityException {
        return new SerialPort(new File(path),baudRate,0);
    }

    public void closeSerialPort(SerialPort serialPort){
        serialPort.close();
    }

}
