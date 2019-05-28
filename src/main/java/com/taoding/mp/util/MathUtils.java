package com.taoding.mp.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * @author: shaobo.qiao
 * @Description 数学计算工具类
 * @date: 2019/3/15 15:41
 */
public class MathUtils {

    public static String countPercent(double dividend,double divisor, int maximumFractionDigits){
        //0表示的是小数点
        DecimalFormat df = new DecimalFormat("0%");
        //可以设置精确几位小数
        df.setMaximumFractionDigits(maximumFractionDigits);
        //模式 例如四舍五入
        df.setRoundingMode(RoundingMode.HALF_UP);
        double quotient = dividend / divisor;
        return df.format(quotient);
    }


}
