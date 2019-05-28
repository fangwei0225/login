/**
 * Copyright © 2018, LeonKeh
 * <p>
 * All Rights Reserved.
 */

package com.taoding.mp.base.service.impl;

import com.taoding.mp.base.dao.LogRepository;
import com.taoding.mp.base.entity.Log;
import com.taoding.mp.base.model.UserSession;
import com.taoding.mp.base.service.LogService;
import com.taoding.mp.util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 日志 Service
 *
 * @author Leon
 * @version 2018/12/10 16:00
 */
@Service
public class LogServiceImpl implements LogService {


    @Autowired
    private LogRepository logRepository;


    /**
     * 保存
     *
     * @param log
     * @return
     */
    @Override
    public Log save(Log log) {
        UserSession userSession = UserSession.getUserSession();
        log.setId(CommonUtils.getUUID());
        log.setCreateTime(CommonUtils.getStringDate(new Date()));
        if (null != userSession) {
            log.setOperator(userSession.getUserId());
            log.setOperatorName(userSession.getUsername());
            log.setCorpId(null == userSession.getCorpId() ? "无所属CorpId" : userSession.getCorpId());
        }
        return logRepository.save(log);
    }
}
