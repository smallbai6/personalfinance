package com.personalfinance.app.Util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeFormatUtil {
    public static String LongToStringA(long date) {
        Date dateOld = new Date(date); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm EE").format(dateOld);// 把date类型的时间转换为string
        return sDateTime;
    }
    public static String LongToStringB(long date) {
        Date dateOld = new Date(date); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = new SimpleDateFormat("yyyy-MM-dd").format(dateOld);// 把date类型的时间转换为string
        return sDateTime;
    }
}
