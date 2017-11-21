package com.skycaster.usbconnectorforwh.utils;

import java.math.BigDecimal;

/**
 * Created by 廖华凯 on 2017/11/15.
 */

public class NumberFormatter {

    public static double getDouble(int integer,int fraction,int scale) throws NumberFormatException{
        if(fraction>=100){
            throw new NumberFormatException("The Fraction Part of a param cannot exceed 99!");
        }
        double temp=fraction*1.d/100;
        double result=integer+temp;
        return new BigDecimal(result).setScale(scale, BigDecimal.ROUND_HALF_EVEN).doubleValue();
    }
}
