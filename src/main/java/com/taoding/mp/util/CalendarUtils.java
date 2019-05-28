package com.taoding.mp.util;

import java.util.Calendar;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/5/7 000717:43
 */
public class CalendarUtils {

    /**
     * 获取一年以内的所有周六周天
     * @param year
     * @return
     */
    public static Set<String> getYearDoubleWeekend(int year){
        Set<String> listDates = new HashSet<String>();
        Calendar calendar=Calendar.getInstance();//当前日期
        calendar.set(year, 6, 1);
        Calendar nowyear=Calendar.getInstance();
        Calendar nexty=Calendar.getInstance();
        nowyear.set(year, 0, 1);//2010-1-1
        nexty.set(year+1, 0, 1);//2011-1-1
        calendar.add(Calendar.DAY_OF_MONTH, -calendar.get(Calendar.DAY_OF_WEEK));//周六
        Calendar c=(Calendar) calendar.clone();
        for(;calendar.before(nexty)&&calendar.after(nowyear);calendar.add(Calendar.DAY_OF_YEAR, -7)){
            listDates.add(calendar.get(Calendar.YEAR)+"-"+(1+calendar.get(Calendar.MONTH))+"-"+calendar.get(Calendar.DATE));
            listDates.add(calendar.get(Calendar.YEAR)+"-"+(1+calendar.get(Calendar.MONTH))+"-"+(1+calendar.get(Calendar.DATE)));
        }
        for(;c.before(nexty)&&c.after(nowyear);c.add(Calendar.DAY_OF_YEAR, 7)){
            listDates.add(c.get(Calendar.YEAR)+"-"+(1+c.get(Calendar.MONTH))+"-"+c.get(Calendar.DATE));
            listDates.add(c.get(Calendar.YEAR)+"-"+(1+c.get(Calendar.MONTH))+"-"+(1+c.get(Calendar.DATE)));
        }

        Set<String> collect = listDates.stream().sorted(Comparator.comparing(String::hashCode)).collect(Collectors.toSet());
        return collect;
    }




}
