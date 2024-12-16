package io.metersphere.sdk.util;

import java.text.DecimalFormat;

public class CalculateUtils {

    // 报告所需的百分比计算
    public static String reportPercentage(int numerator, int denominator) {
        if (denominator == 0) {
            return "0.00%";
        }
        DecimalFormat rateFormat = new DecimalFormat("#0.00");
        rateFormat.setMinimumFractionDigits(2);
        rateFormat.setMaximumFractionDigits(2);

        double passRate = percentage(numerator, denominator);
        if (passRate == 100 && numerator < denominator) {
            return "99.99%";
        } else if (passRate == 0 && numerator > 0) {
            return "0.01%";
        } else {
            return rateFormat.format(passRate) + "%";
        }
    }


    public static double percentage(int numerator, int denominator) {
        if (denominator == 0) {
            return 0;
        }
        DecimalFormat rateFormat = new DecimalFormat("#0.00");
        rateFormat.setMinimumFractionDigits(2);
        rateFormat.setMaximumFractionDigits(2);

        return Double.parseDouble(rateFormat.format((double) numerator * 100 / (double) denominator));
    }
}
