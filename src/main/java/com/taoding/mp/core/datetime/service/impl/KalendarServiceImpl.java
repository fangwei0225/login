package com.taoding.mp.core.datetime.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.taoding.mp.base.dao.BaseDAO;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.core.datetime.dao.KalendarRepository;
import com.taoding.mp.core.datetime.entity.Kalendar;
import com.taoding.mp.core.datetime.service.KalendarService;
import com.taoding.mp.util.CreateObjUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author liuxinghong
 * @Description:
 * @date 2019/5/7 000718:53
 */
@Slf4j
@Service
@Transactional
public class KalendarServiceImpl  extends BaseDAO implements KalendarService {

    @Autowired
    private KalendarRepository kalendarRepository;

    //初始化一年中所有的日期周六周日为节假日
    public  List<Kalendar> initWorkdays(Integer year){
        List<Kalendar> dayList = Lists.newLinkedList();
        Calendar calendars = Calendar.getInstance(); //当前日期
       // int year = calendars.get(Calendar.YEAR);
        Calendar calendar = new GregorianCalendar(year,0,1);
        int i=1;
        while(calendar.get(Calendar.YEAR)<year+1){
            calendar.set(Calendar.WEEK_OF_YEAR,i++);
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            if(calendar.get(Calendar.YEAR)==year){
               // String date = CommonUtils.DateToString(calendar.getTime(),"yyyy-MM-dd");
                Date date = calendar.getTime();
                LocalDateTime dateTime = new LocalDateTime(date);
                String time = dateTime.toString(Constants.YYYY_MM_DD);
                Kalendar kalendar = CreateObjUtils.create(Kalendar.class);
                kalendar.setDate(time);
                kalendar.setType(Constants.DAY_WEEKEND);
                kalendar.setYear(year);
                kalendar.setWeek(Calendar.SUNDAY);
                dayList.add(kalendar);
            }
            calendar.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
            if(calendar.get(Calendar.YEAR)==year){
               // String date = CommonUtils.DateToString(calendar.getTime(),"yyyy-MM-dd");

                Date date = calendar.getTime();
                LocalDateTime dateTime = new LocalDateTime(date);
                String time = dateTime.toString(Constants.YYYY_MM_DD);
                Kalendar kalendar = CreateObjUtils.create(Kalendar.class);
                kalendar.setDate(time);
                kalendar.setType(Constants.DAY_WEEKEND);
                kalendar.setYear(year);
                kalendar.setWeek(Calendar.SATURDAY);
                dayList.add(kalendar);

            }
            calendar.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
            if(calendar.get(Calendar.YEAR)==year){
                //String date = CommonUtils.DateToString(calendar.getTime(),"yyyy-MM-dd");

                Date date = calendar.getTime();
                LocalDateTime dateTime = new LocalDateTime(date);
                String time = dateTime.toString(Constants.YYYY_MM_DD);
                Kalendar kalendar = CreateObjUtils.create(Kalendar.class);
                kalendar.setDate(time);
                kalendar.setType(Constants.DAY_WORK);
                kalendar.setYear(year);
                kalendar.setWeek(Calendar.MONDAY);
                dayList.add(kalendar);

            }
            calendar.set(Calendar.DAY_OF_WEEK,Calendar.THURSDAY);
            if(calendar.get(Calendar.YEAR)==year){
               // String date = CommonUtils.DateToString(calendar.getTime(),"yyyy-MM-dd");

                Date date = calendar.getTime();
                LocalDateTime dateTime = new LocalDateTime(date);
                String time = dateTime.toString(Constants.YYYY_MM_DD);
                Kalendar kalendar = CreateObjUtils.create(Kalendar.class);
                kalendar.setDate(time);
                kalendar.setType(Constants.DAY_WORK);
                kalendar.setYear(year);
                kalendar.setWeek(Calendar.THURSDAY);
                dayList.add(kalendar);

            }
            calendar.set(Calendar.DAY_OF_WEEK,Calendar.TUESDAY);
            if(calendar.get(Calendar.YEAR)==year){
               // String date = CommonUtils.DateToString(calendar.getTime(),"yyyy-MM-dd");

                Date date = calendar.getTime();
                LocalDateTime dateTime = new LocalDateTime(date);
                String time = dateTime.toString(Constants.YYYY_MM_DD);
                Kalendar kalendar = CreateObjUtils.create(Kalendar.class);
                kalendar.setDate(time);
                kalendar.setType(Constants.DAY_WORK);
                kalendar.setYear(year);
                kalendar.setWeek(Calendar.TUESDAY);
                dayList.add(kalendar);


            }
            calendar.set(Calendar.DAY_OF_WEEK,Calendar.FRIDAY);
            if(calendar.get(Calendar.YEAR)==year){
                //String date = CommonUtils.DateToString(calendar.getTime(),"yyyy-MM-dd");

                Date date = calendar.getTime();
                LocalDateTime dateTime = new LocalDateTime(date);
                String time = dateTime.toString(Constants.YYYY_MM_DD);
                Kalendar kalendar = CreateObjUtils.create(Kalendar.class);
                kalendar.setDate(time);
                kalendar.setType(Constants.DAY_WORK);
                kalendar.setYear(year);
                kalendar.setWeek(Calendar.FRIDAY);
                dayList.add(kalendar);


            }
            calendar.set(Calendar.DAY_OF_WEEK,Calendar.WEDNESDAY);
            if(calendar.get(Calendar.YEAR)==year){
               // String date = CommonUtils.DateToString(calendar.getTime(),"yyyy-MM-dd");

                Date date = calendar.getTime();
                LocalDateTime dateTime = new LocalDateTime(date);
                String time = dateTime.toString(Constants.YYYY_MM_DD);
                Kalendar kalendar = CreateObjUtils.create(Kalendar.class);
                kalendar.setDate(time);
                kalendar.setType(Constants.DAY_WORK);
                kalendar.setYear(year);
                kalendar.setWeek(Calendar.WEDNESDAY);
                dayList.add(kalendar);

            }
        }
    return dayList;
    }
    @Override
    public List<Kalendar> list(Integer year) {
       return kalendarRepository.findByYearAndIsDelete(year,Constants.STATUE_NORMAL);
    }

    @Override
    public boolean add(Integer year,String dates) {
        List<String> dateList = Splitter.on(",").trimResults().splitToList(dates);
        //判断该年度的日期是否存在
        List<Kalendar> kalendars = this.initWorkdays(Integer.valueOf(year));
        List<Kalendar> list = kalendarRepository.findByYearAndIsDelete(Integer.valueOf(year), Constants.STATUE_NORMAL);
        if (CollectionUtils.isEmpty(list)){
            //新加操作
            kalendars.forEach(item->{
                if (dateList.contains(item.getDate())){
                    if (Constants.DAY_WORK.equals(item.getType())){
                        item.setType(Constants.DAY_HOLIDAY);
                    }
                    if (Constants.DAY_WEEKEND.equals(item.getType())){
                        item.setType(Constants.DAY_WORK);
                    }
                    item.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
                }
            });
            kalendarRepository.saveAll(kalendars);
        }else {
         //修改操作
            LinkedList<Kalendar> resultList = Lists.newLinkedList();
            list.forEach(item->{
            if (dateList.contains(item.getDate())&&Constants.DAY_WORK.equals(item.getType())&& Constants.DAY_WORK.equals(getKalendarByDate(kalendars,item.getDate()).getType())){//工作日添加节假日
                item.setType(Constants.DAY_HOLIDAY);
            }
            if (!dateList.contains(item.getDate())&&Constants.DAY_HOLIDAY.equals(item.getType())&& Constants.DAY_WORK.equals(getKalendarByDate(kalendars,item.getDate()).getType())){//工作日删除节假日
                item.setType(Constants.DAY_WORK);
            }
            if (!dateList.contains(item.getDate())&& Constants.DAY_WEEKEND.equals(item.getType())&& Constants.DAY_WEEKEND.equals(getKalendarByDate(kalendars,item.getDate()).getType())){//添加周末为工作日
                item.setType(Constants.DAY_WORK);
            }
            if (dateList.contains(item.getDate())&& Constants.DAY_WORK.equals(item.getType()) && Constants.DAY_WEEKEND.equals(getKalendarByDate(kalendars,item.getDate()).getType())){//将周末的工作日变为周末日
                item.setType(Constants.DAY_WEEKEND);
            }
                item.setUpdateTime(DateTime.now().toString(Constants.YYYY_MM_DD_HH_MM_SS));
                resultList.add(item);
            });
            kalendarRepository.saveAll(resultList);
        }
                return true;
    }


   private Kalendar getKalendarByDate(List<Kalendar> kalendars,String date){
       for (Kalendar item:kalendars){
            if (date.equals(item.getDate())){
                return item;
            }
        }
        return null;
   }

    @Override
    public List<Kalendar> allList(Integer type) {
      return   kalendarRepository.findByTypeAndIsDelete(type,Constants.STATUE_NORMAL);
    }
}
