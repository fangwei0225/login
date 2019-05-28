package com.taoding.mp.base.aspect;

import com.alibaba.fastjson.JSON;
import com.taoding.mp.base.entity.Log;
import com.taoding.mp.base.service.LogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 系统操作日志切面类
 *
 * @author Leon
 * @version 2018/12/10 17:40
 */
@Aspect
@Component
public class LogAspect {

    @Autowired
    private LogService logService;

    private ThreadLocal<Long> time = new ThreadLocal<>();

    private static final Logger lg = LoggerFactory.getLogger(LogAspect.class);

    @Pointcut("execution(public * com.taoding.mp.*.service.*.*(..)) && !execution(* com.taoding.mp.base.service.LogService.*(..)) " +
            "&& !execution(* com.taoding.mp.base.service.ConfigService.*(..)) || execution(public * com.taoding.mp.core.*.service.*.*(..))")
    public void log() {
    }

    @Pointcut("execution(public * com.taoding.mp.*.controller.*.*(..)) || execution(public * com.taoding.mp.core.*.controller.*.*(..))")
    public void webTime() {
    }

    @Around("log()")
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable {
        Object obj = joinPoint.proceed();
        writeSystemLog(joinPoint);
        return obj;
    }

    @Before("webTime()")
    public void before(JoinPoint joinPoint) {
        time.set(System.currentTimeMillis());
    }

    @AfterReturning("webTime()")
    public void afterReturn(JoinPoint joinPoint) {
        long endTime = System.currentTimeMillis();
        lg.info("方法【{}】执行时间：【{}】ms", joinPoint.getTarget().getClass().getSimpleName() + "." + joinPoint.getSignature().getName(), endTime - time.get());
        time.remove();
    }

    /**
     * Extract some message of current method, then save the log
     *
     * @param joinPoint
     */
    private void writeSystemLog(ProceedingJoinPoint joinPoint) {
        Log log = new Log();
        log.setType(joinPoint.getTarget().getClass().getSimpleName() + "." + joinPoint.getSignature().getName());
        log.setContent(JSON.toJSONString(joinPoint.getArgs()));
        logService.save(log);
    }
}
