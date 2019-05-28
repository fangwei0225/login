/**
 * Copyright © 2018, LeonKeh
 * <p>
 * All Rights Reserved.
 */

package com.taoding.mp.base.dao;

import com.taoding.mp.base.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 日志 Repository
 *
 * @author Leon
 * @version 2018/12/10 17:32
 */
public interface LogRepository extends JpaRepository<Log, String> {
}
