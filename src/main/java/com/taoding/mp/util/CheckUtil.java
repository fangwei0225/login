package com.taoding.mp.util;

import com.taoding.mp.core.execption.CustomException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: youngsapling
 * @date: 2019-05-22
 * @modifyTime:
 * @description:
 */
public class CheckUtil {
    public static void isBlank(String value, Integer code, String message) {
        if (StringUtils.isBlank(value)){
            throw new CustomException(code, message);
        }
    }
}
