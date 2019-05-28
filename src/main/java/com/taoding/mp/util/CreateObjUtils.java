package com.taoding.mp.util;

import com.taoding.mp.base.model.BaseEntity;
import com.taoding.mp.commons.Constants;
import com.taoding.mp.core.execption.CustomException;

import java.util.Date;
/**
 * @author: youngsapling
 * @date: 2019-04-15
 * @modifyTime:
 * @description:
 */
public class CreateObjUtils {

    public static <T> T addBase(T baseEntity){
        String stringDate = CommonUtils.getStringDate(new Date());
        if(baseEntity instanceof BaseEntity){
            BaseEntity temp = (BaseEntity) baseEntity;
            temp.setId(IdWorker.createId());
            temp.setCreateTime(stringDate);
            temp.setUpdateTime(stringDate);
            temp.setIsDelete(1);
            temp.setCorpId(Constants.CORP_ID);
            return (T) temp;
        }else {
            return baseEntity;
        }
    }

    public static <T> T create(Class<T> tClass){
        try {
            T t = tClass.newInstance();
            if(!(t instanceof BaseEntity)){
                return null;
            }
            BaseEntity father = (BaseEntity) t;
            String stringDate = CommonUtils.getStringDate(new Date());
            father.setId(IdWorker.createId());
            father.setCreateTime(stringDate);
            father.setUpdateTime(stringDate);
            father.setIsDelete(1);
            father.setCorpId(Constants.CORP_ID);
            return (T) father;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new CustomException(500, "创建对象失败.");
    }
}
