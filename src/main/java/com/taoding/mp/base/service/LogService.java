/**
 * Copyright © 2018, LeonKeh
 * <p>
 * All Rights Reserved.
 */

package com.taoding.mp.base.service;

import com.taoding.mp.base.entity.Log;

/**
 * 日志 Service
 *
 * @author Leon
 * @version 2018/12/10 16:00
 */
public interface LogService {

    /**
     * 保存
     *
     * @param log
     * @return
     */
    Log save(Log log);
}
