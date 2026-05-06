package com.neobank.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.neobank.service.*.*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            log.info("→ {}.{}() started", className, methodName);
            Object result = joinPoint.proceed();
            stopWatch.stop();
            log.info("← {}.{}() completed in {}ms",
                    className, methodName, stopWatch.getTotalTimeMillis());
            return result;

        } catch (Exception ex) {
            stopWatch.stop();
            log.error("✗ {}.{}() failed after {}ms — {}: {}",
                    className, methodName,
                    stopWatch.getTotalTimeMillis(),
                    ex.getClass().getSimpleName(),
                    ex.getMessage());
            throw ex;
        }
    }
}
