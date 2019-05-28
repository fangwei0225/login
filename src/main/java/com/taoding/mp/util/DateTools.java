package com.taoding.mp.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @author : liuxinghong
 * Date: 2019-04-08
 * Time: 10:24
 */
@Component
public class DateTools {

    private static JdbcTemplate jdbc;

    /**
     * 日期转字符串
     * @param date
     * @return
     */
    public static String dateToStr(Date date){
        return new DateTime(date).toString("yyyy-MM-dd HH:mm:ss");
    }

    public static String dateToStrForYearsAndMonth(Date date){
        return new DateTime(date).toString("yyyy-MM");
    }

    /**
     * 获取当前时间
     * @return
     */
    public static Date currentTime(){
        return DateTime.now().toDate();
    }

    /**
     * 获取当前时间之前的日期的起点
     * @param days
     * @return
     */
    public static Date getBeforDay(String days) {
        int daysInt = Integer.parseInt(days);
        Calendar canlendar = Calendar.getInstance(); // java.util包
        canlendar.add(Calendar.DATE, daysInt); // 日期减 如果不够减会将月变动
        canlendar.set(canlendar.get(Calendar.YEAR),canlendar.get(Calendar.MONTH),canlendar.get(Calendar.DAY_OF_MONTH),0,0,0);
        return canlendar.getTime();
    }

    /**
     * 获取当天的开始时间
     * @return
     */
    public static Date getDayBegin() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    /**
     * 获取当天的结束时间
     * @return
     */
    public static Date getDayEnd() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    /**
     * 获取昨天的开始时间
     * @return
     */
    public static Date getBeginDayOfYesterday() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(getDayBegin());
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }
    /**
     * 获取昨天的结束时间
     * @return
     */
    public static Date getEndDayOfYesterDay() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(getDayEnd());
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }

    /**
     * 获取明天的开始时间
     * @return
     */
    public static Date getBeginDayOfTomorrow() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(getDayBegin());
        cal.add(Calendar.DAY_OF_MONTH, 1);

        return cal.getTime();
    }

    /**
     * 获取明天的结束时间
     * @return
     */
    public static Date getEndDayOfTomorrow() {
        Calendar cal = new GregorianCalendar();
        cal.setTime(getDayEnd());
        cal.add(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    /**
     * 获取本周的开始时间
     * @return
     */
    @SuppressWarnings("unused")
    public static String getBeginDayOfWeek() {
        Date date = new Date();
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayofweek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayofweek == 1) {
            dayofweek += 7;
        }
        cal.add(Calendar.DATE, 2 - dayofweek);
        return getDayStartTime(cal.getTime());
    }

    /**
     * 获取本周的开始时的日期时间
     * @return Date
     */
    public static Date getBeginDayOfWeekDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return cal.getTime();
    }

    /**
     * 获取本周的结束时的日期时间
     * @return Date
     */
    public static Date getEndDayOfWeekDate() {
        Calendar endDayCal = Calendar.getInstance();
        endDayCal.setTime(getBeginDayOfWeekDate());
        endDayCal.add(Calendar.DAY_OF_WEEK, 6);
        endDayCal.set(Calendar.HOUR_OF_DAY, 23);
        endDayCal.set(Calendar.MINUTE, 59);
        endDayCal.set(Calendar.SECOND, 59);
        return  endDayCal.getTime();
    }

    /**
     * 获取上周的开始日期
     * @return
     */
    public static Date getBeginOfLastWeek(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(getBeginDayOfWeekDate());
        cal.add(Calendar.DATE, -7);
        return cal.getTime();
    }

    /**
     * 获取上周的结束日期
     * @return
     */
    public static Date getEndOfLastWeek(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(getEndDayOfWeekDate());
        cal.add(Calendar.DATE, -7);
        return cal.getTime();
    }

    /**
     * 获取本月的开始时间
     * @return
     */
    public static Date getBeginDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth() - 1, 1,0,0,0);
        return calendar.getTime();
    }
    /**
     * 获取本月的结束时间
     * @return
     */
    public static Date getEndDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth() - 1, 1);
        int day = calendar.getActualMaximum(5);
        calendar.set(getNowYear(), getNowMonth() - 1, day,23,59,59);
        return calendar.getTime();
    }
    /**
     * 获取上月的开始时间
     * @return
     */
    public static Date getBeginDayOfLastMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),    calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        return calendar.getTime();
    }

    /**
     * 获取上月的结束时间
     * @return
     */
    public static Date getEndDayOfLastMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth() - 2, 1);
        int day = calendar.getActualMaximum(5);
        calendar.set(getNowYear(), getNowMonth() - 2, day,23,59,59);
        return calendar.getTime();
    }

    /**
     * 获取本年的开始时间
     * @return
     */
    public static String getBeginDayOfYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, getNowYear());
        // cal.set
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);

        return getDayStartTime(cal.getTime());
    }

    /**
     * 获取本年的结束时间
     * @return
     */
    public static Date getEndDayOfYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, getNowYear());
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DATE, 31);
        return cal.getTime();
    }

    /**
     * 获取某个日期的开始时间
     * @param d
     * @return
     */
    public static String getDayStartTime(Date d) {
        Calendar calendar = Calendar.getInstance();
        if(null != d) {
            calendar.setTime(d);
        }
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),    calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date date = new Timestamp(calendar.getTimeInMillis());
        return new DateTime(date).toString("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 根据指定date获取本月开始时间
     * @param d
     * @return
     */
    public static String getDayStartDays(Date d) {
        Calendar calendar = Calendar.getInstance();
        if(null != d) {
            calendar.setTime(d);
        }
        System.out.println(calendar.get(Calendar.YEAR));
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),    calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        Date date = new Timestamp(calendar.getTimeInMillis());
        return new DateTime(date).toString("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 根据指定date获取本月结束时间
     * @param d
     * @return
     */
    public static String getDayEndDays(Date d) {
        Calendar calendar = Calendar.getInstance();
        if(null != d){
            calendar.setTime(d);
        }
        //calendar.set(getNowYear(), getNowMonth() - 1, 1);
        int day = calendar.getActualMaximum(5);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), day,23,59,59);
        Date date = new Timestamp(calendar.getTimeInMillis());
        return new DateTime(date).toString("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 获取指定date上月的开始时间
     * @return
     */
    public static String getBeforBeginDayOfDate(Date d) {
        Calendar calendar = Calendar.getInstance();
        if(null != d){
            calendar.setTime(d);
        }
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) - 1, 1,0,0,0);
       // calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.DAY_OF_MONTH) - 2, 1,0,0,0);
        Date date = new Timestamp(calendar.getTimeInMillis());
        return new DateTime(date).toString("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 获取指定date上月的结束时间
     * @return
     */
    public static String getBeforEndDayOfDate(Date d) {

        Calendar calendar = Calendar.getInstance();
        if(null != d){
            calendar.setTime(d);
        }
        calendar.set(2,calendar.get(Calendar.MONTH)-1);
        int day = calendar.getActualMaximum(5);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), day,23,59,59);
        Date date = new Timestamp(calendar.getTimeInMillis());
        return new DateTime(date).toString("yyyy-MM-dd HH:mm:ss");
    }


    /**
     * 获取某个日期的结束时间
     * @param d
     * @return
     */
    public static Date getDayEndTime(Date d) {
        Calendar calendar = Calendar.getInstance();
        if(null != d){
            calendar.setTime(d);
        }
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),    calendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date date = new Timestamp(calendar.getTimeInMillis());
        return date;
    }

    /**
     * 获取今年是哪一年
     * @return
     */
    public static Integer getNowYear() {
        Date date = new Date();
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        return Integer.valueOf(gc.get(1));
    }

    /**
     * 获取本月是哪一月
     * @return
     */
    public static int getNowMonth() {
        Date date = new Date();
        GregorianCalendar gc = (GregorianCalendar) Calendar.getInstance();
        gc.setTime(date);
        return gc.get(2) + 1;
    }
    /**
     * 两个日期相减得到的天数
     * @param beginDate
     * @param endDate
     * @return
     */
    public static int getDiffDays(Date beginDate, Date endDate) {

        if (beginDate == null || endDate == null) {
            throw new IllegalArgumentException("getDiffDays param is null!");
        }

        long diff = (endDate.getTime() - beginDate.getTime())
                / (1000 * 60 * 60 * 24);

        int days = new Long(diff).intValue();

        return days;
    }

    /**
     * 两个日期相减得到的毫秒数
     * @param beginDate
     * @param endDate
     * @return
     */
    public static long dateDiff(Date beginDate, Date endDate) {
        long date1ms = beginDate.getTime();
        long date2ms = endDate.getTime();
        return date2ms - date1ms;
    }

    /**
     * 获取两个日期中的最大日期
     * @param beginDate
     * @param endDate
     * @return
     */
    public static Date max(Date beginDate, Date endDate) {
        if (beginDate == null) {
            return endDate;
        }
        if (endDate == null) {
            return beginDate;
        }
        if (beginDate.after(endDate)) {
            return beginDate;
        }
        return endDate;
    }

    /**
     * 获取两个日期中的最小日期
     * @param beginDate
     * @param endDate
     * @return
     */
    public static Date min(Date beginDate, Date endDate) {
        if (beginDate == null) {
            return endDate;
        }
        if (endDate == null) {
            return beginDate;
        }
        if (beginDate.after(endDate)) {
            return endDate;
        }
        return beginDate;
    }

    /**
     * 返回某月该季度的第一个月
     * @param date
     * @return
     */
    public static Date getFirstSeasonDate(Date date) {
        final int[] SEASON = { 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4 };
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int sean = SEASON[cal.get(Calendar.MONTH)];
        cal.set(Calendar.MONTH, sean * 3 - 3);
        return cal.getTime();
    }

    /**
     * 返回某个日期下几天的日期
     * @param date
     * @param i
     * @return
     */
    public static Date getNextDay(Date date, int i) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.DATE, cal.get(Calendar.DATE) + i);
        return cal.getTime();
    }

    /**
     * 返回某个日期前几天的日期
     * @param date
     * @param i
     * @return
     */
    public static Date getFrontDay(Date date, int i) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.DATE, cal.get(Calendar.DATE) - i);
        return cal.getTime();
    }

    /**
     * 获取某年某月到某年某月按天的切片日期集合（间隔天数的集合）
     * @param beginYear
     * @param beginMonth
     * @param endYear
     * @param endMonth
     * @param k
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List getTimeList(int beginYear, int beginMonth, int endYear,
                                   int endMonth, int k) {
        List list = new ArrayList();
        if (beginYear == endYear) {
            for (int j = beginMonth; j <= endMonth; j++) {
                list.add(getTimeList(beginYear, j, k));

            }
        } else {
            {
                for (int j = beginMonth; j < 12; j++) {
                    list.add(getTimeList(beginYear, j, k));
                }

                for (int i = beginYear + 1; i < endYear; i++) {
                    for (int j = 0; j < 12; j++) {
                        list.add(getTimeList(i, j, k));
                    }
                }
                for (int j = 0; j <= endMonth; j++) {
                    list.add(getTimeList(endYear, j, k));
                }
            }
        }
        return list;
    }

    /**
     * 获取某年某月按天切片日期集合（某个月间隔多少天的日期集合）
     * @param beginYear
     * @param beginMonth
     * @param k
     * @return
     */
    public static List getTimeList(int beginYear, int beginMonth, int k) {
        List list = new ArrayList();
        Calendar begincal = new GregorianCalendar(beginYear, beginMonth, 1);
        int max = begincal.getActualMaximum(Calendar.DATE);
        for (int i = 1; i < max; i = i + k) {
            list.add(begincal.getTime());
            begincal.add(Calendar.DATE, k);
        }
        begincal = new GregorianCalendar(beginYear, beginMonth, max);
        list.add(begincal.getTime());
        return list;
    }

    /**
     * 获取当前月的差额月
     * @param amount
     * @return
     */
    public static String getAmountYearMonthStr(Integer amount){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, amount);
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM");
        String time = format.format(c.getTime());
        return time;
    }

    /**
     * 根据当前指定的年月获取上一个月
     * @param date
     * @return
     */
    public static Date getBeforeMonth(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        return cal.getTime();
    }

    /**
     * 根据指定月获取上个月的年月字符串（yyyy-MM）
     * @param date
     * @return
     */
    public static String getBeforeYearMonthStr(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM");
        String time = format.format(cal.getTime());
        return time;
    }

    /**
     * Transfer a date to localDate
     *
     * @param date date to transfer
     * @return the localDate of date
     */
    public static LocalDate dateToLocalDate(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Transfer a localDate to date
     *
     * @param localDate localDate to transfer
     * @return the date of localDate
     */
    public static Date localDateToDate(LocalDate localDate) {
        Instant instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    /**
     * Predicating that is it weekend the date
     *
     * @param date date
     * @return
     */
    public static boolean isWeekEnd(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }

    /**
     * Calculates the interval of between startDate and endDate
     *
     * @param startDate a date to use
     * @param endDate a date to use
     * @return
     */
    public static int getWorkDayBetween(Date startDate, Date endDate) {
        LocalDate startDateTime = DateTools.dateToLocalDate(startDate);
        LocalDate endDateTime = DateTools.dateToLocalDate(endDate);
        if (startDateTime.isAfter(endDateTime)) {
            throw new IllegalArgumentException("EndDate must be later than startDate!");
        }
        List<LocalDate> list = getLocalDateList();
        int interval = 0;
        do {
            if (list.contains(startDateTime) || !DateTools.isWeekEnd(startDateTime)) {
                interval++;
            }
            startDateTime = startDateTime.plusDays(1);
        } while (!startDateTime.isAfter(endDateTime));
        return interval;
    }

    /**
     * Get an date for the given offset
     *
     * @param baseDate baseDate
     * @param offset offset
     * @return
     */
    public static Date getDateWithSkipHoliday(Date baseDate, int offset) {
        if (0 == offset) {
            return baseDate;
        }
        int oldOffset = offset;
        LocalDate baseDateTime = DateTools.dateToLocalDate(baseDate);
        List<LocalDate> list = getLocalDateList();
        while (0 != oldOffset) {
            if (0 < oldOffset) {
                if (list.contains(baseDateTime) || !DateTools.isWeekEnd(baseDateTime)) {
                    oldOffset--;
                }
                baseDateTime = baseDateTime.plusDays(1);
            } else {
                if (list.contains(baseDateTime) || !DateTools.isWeekEnd(baseDateTime)) {
                    oldOffset++;
                }
                baseDateTime = baseDateTime.plusDays(-1);
            }
        }
        if (0 < offset) {
            baseDateTime = baseDateTime.plusDays(-1);
        } else {
            baseDateTime = baseDateTime.plusDays(1);
        }
        return DateTools.localDateToDate(baseDateTime);
    }

    /**
     * 从数据加载节假日信息
     *
     * @return
     */
    private static List<LocalDate> getLocalDateList() {
        String sql = "SELECT `date` FROM kalendar WHERE type = 0";
        List<String> dateList = jdbc.queryForList(sql, String.class);
        return dateList.stream().map(DateTools::stringToLocalDate).collect(Collectors.toList());
    }

    /**
     * A string date to localDate
     *
     * @param date a string date to transfer
     * @return
     */
    public static LocalDate stringToLocalDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Autowired
    public void setProjectProperties(JdbcTemplate jdbc) {
        DateTools.jdbc = jdbc;
    }

    public static void main(String[] args){
        System.out.println("当前时间："+dateToStr(currentTime()));
        System.out.println("昨天开始时间："+dateToStr(getBeginDayOfYesterday()));
        System.out.println("昨天开始结束："+dateToStr(getEndDayOfYesterDay()));
        System.out.println("当天开始时间："+dateToStr(getDayBegin()));
        System.out.println("当天开始结束："+dateToStr(getDayEnd()));
        System.out.println("本月开始结束："+dateToStr(getBeginDayOfMonth()));
        System.out.println("本月开始结束："+dateToStr(getEndDayOfMonth()));
        System.out.println("上月开始结束："+dateToStr(getBeginDayOfLastMonth()));
        System.out.println("上月开始结束："+dateToStr(getEndDayOfLastMonth()));
        System.out.println("获取当前时间n天之前的日期的起点："+dateToStr(getBeforDay("-25")));
        System.out.println("获取当前月的差额月(yyyy-MM)："+getAmountYearMonthStr(-12));
        System.out.println("根据本月获取上月："+dateToStr(getBeforeMonth(new Date())));
        System.out.println("上周开始时间：" + dateToStr(getBeginOfLastWeek()));
        System.out.println("上周结束时间："+ dateToStr(getEndOfLastWeek()));
        System.out.println(getBeforBeginDayOfDate(DateTime.parse("2016-02", DateTimeFormat.forPattern("yyyy-MM")).toDate()));
        System.out.println(getBeforEndDayOfDate(DateTime.parse("2016-02", DateTimeFormat.forPattern("yyyy-MM")).toDate()));
        System.out.println(getDayStartDays(DateTime.parse("2016-02", DateTimeFormat.forPattern("yyyy-MM")).toDate()));
        System.out.println(getDayEndDays(DateTime.parse("2016-02", DateTimeFormat.forPattern("yyyy-MM")).toDate()));

    }
}
