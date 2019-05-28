/**
 * Copyright © 2018, Leon
 * <p>
 * All Rights Reserved.
 */

package com.taoding.mp.base.dao;

import com.taoding.mp.base.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 消息 Repository
 *
 * @author Leon
 * @version 2019/04/17 16:37
 */
public interface MessageRepository extends JpaRepository<Message, String> {

}