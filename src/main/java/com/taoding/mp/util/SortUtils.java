package com.taoding.mp.util;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author shaobo.qiao
 * @Date 2019/3/22 17:49
 **/
public class SortUtils {

    public static final String DESC = "desc";
    public static final String ASC = "asc";


    /**
     * List<Map<String,Object>>排序
     *
     * @Description 对List<Map   <   String   ,   Object>>排序，根据指定的Map.key对应的value排序，支持排序的Object类型为基本类型、String或实现了Comparable接口的类对象，超出此类型范围的按照String类型比较
     * @Author shaobo.qiao
     * @Date 2019/3/22 18:06
     * @Param [list, field, sort]
     * field为Map.key的值
     * sort 排序规则,desc/asc
     **/
    public static List<Map<String, Object>> sort(List<Map<String, Object>> list, String field, final String sort) {
        Collections.sort(list, (o1, o2) -> {
            Object a = o1.get(field);
            Object b = o2.get(field);
            int ret = 0;
            try {
                Class<?> type = a.getClass();

                if (type == int.class) {
                    ret = ((Integer) a).compareTo((Integer) b);
                } else if (type == double.class) {
                    ret = ((Double) a).compareTo((Double) b);
                } else if (type == long.class) {
                    ret = ((Long) a).compareTo((Long) b);
                } else if (type == float.class) {
                    ret = ((Float) a).compareTo((Float) b);
                } else if (type == Date.class) {
                    ret = ((Date) a).compareTo((Date) b);
                } else if (isImplementsOf(type, Comparable.class)) {
                    ret = ((Comparable) a).compareTo((Comparable) b);
                } else {
                    ret = String.valueOf(a).compareTo(String.valueOf(b));
                }

            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            if (sort != null && sort.toLowerCase().equals(DESC)) {
                return -ret;
            } else {
                return ret;
            }
        });
        return list;
    }


    /**
     * 判断对象实现的所有接口中是否包含szInterface
     *
     * @param clazz
     * @param szInterface
     * @return
     */
    public static boolean isImplementsOf(Class<?> clazz, Class<?> szInterface) {
        boolean flag = false;

        Class<?>[] face = clazz.getInterfaces();
        for (Class<?> c : face) {
            if (c == szInterface) {
                flag = true;
            } else {
                flag = isImplementsOf(c, szInterface);
            }
        }

        if (!flag && null != clazz.getSuperclass()) {
            return isImplementsOf(clazz.getSuperclass(), szInterface);
        }

        return flag;
    }


}

