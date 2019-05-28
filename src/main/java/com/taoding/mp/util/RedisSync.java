package com.taoding.mp.util;

import com.taoding.mp.base.model.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author: youngsapling
 * @date: 2019-04-10
 * @modifyTime:
 * @description:
 */
@Component
@Slf4j
public class RedisSync {
    @Autowired
    StringRedisTemplate stringRedisTemplate;


    private final String PREFIX = "sync:";

    private final String SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    /**
     *
     * @param baseWorkId
     * @param timeOutSeconds
     * @return null or syncFlag
     */
    public String getSync(String baseWorkId, long timeOutSeconds, TimeUnit timeUnit){
        if(StringUtils.isBlank(baseWorkId)){
            log.info("申请加锁的key is null.");
            return null;
        }
        baseWorkId = PREFIX + baseWorkId;
        String owner = IdWorker.createId();
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        Boolean aBoolean = ops.setIfAbsent(baseWorkId, owner, timeOutSeconds, timeUnit);
        if(aBoolean){
            return owner;
        }else {
            return null;
        }
    }

    public void removeSync(String baseWorkId, String owner){
        baseWorkId = PREFIX + baseWorkId;
        releaseLock(baseWorkId, owner);
    }

    private void releaseLock(String lockKey, String owner){
        RedisScript<Long> redisScript = new DefaultRedisScript<>(SCRIPT, Long.class);
        Long result = stringRedisTemplate.execute(redisScript, Collections.singletonList(lockKey), owner);
        if(1 != result) {
            log.info("锁:[{}]释放失败.当前操作人是:[{}], questUri:[{}]", lockKey, UserSession.getUserSession().getName(),
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getRequestURI());
        }
    }
}
