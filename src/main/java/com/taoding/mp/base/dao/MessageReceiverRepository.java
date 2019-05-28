/**
 * Copyright © 2018, Leon
 * <p>
 * All Rights Reserved.
 */

package com.taoding.mp.base.dao;

import com.taoding.mp.base.entity.MessageReceiver;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 消息记录表 Repository
 *
 * @author Leon
 * @version 2019/04/17 16:37
 */
public interface MessageReceiverRepository extends JpaRepository<MessageReceiver, String> {

}