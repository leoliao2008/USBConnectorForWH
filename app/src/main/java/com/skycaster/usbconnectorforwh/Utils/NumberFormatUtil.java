package com.skycaster.usbconnectorforwh.Utils;

import java.math.BigDecimal;

/**
 * Created by 廖华凯 on 2017/11/15.
 */

public class NumberFormatUtil {

    public static double getDouble(int integer,int fraction) throws NumberFormatException{
        if(fraction>=100){
            throw new NumberFormatException("小数部分不能超过100");
        }
        double temp=fraction*1.d/100;
        double result=integer+temp;
        return new BigDecimal(result).setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
    }
}
