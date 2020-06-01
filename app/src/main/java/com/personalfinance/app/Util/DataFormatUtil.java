package com.personalfinance.app.Util;

import java.text.DecimalFormat;

public class DataFormatUtil {
    public static String formatPrice(double price) {
        DecimalFormat df = new DecimalFormat("0.00");
        String format = df.format(price);
        return format;
    }
}
