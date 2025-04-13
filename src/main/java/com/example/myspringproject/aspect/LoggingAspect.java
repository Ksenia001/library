package com.example.myspringproject.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Pointcut("execution(* com.example.myspringproject.service.*(..))")
    public void servicesPointcut() {}

    @Before("servicesPointcut()")
    public void logBefore(JoinPoint joinPoint) {
        if (logger.isInfoEnabled()) {
            logger.info("Executing method: {} with args: {}",
                    joinPoint.getSignature().toShortString(), joinPoint.getArgs());
        }
    }

    @AfterReturning(pointcut = "servicesPointcut()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        if (logger.isInfoEnabled()) {
            logger.info("Method {} executed successfully. Result: {}",
                    joinPoint.getSignature().toShortString(), result);
        }
    }

    @AfterThrowing(pointcut = "servicesPointcut()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        if (logger.isErrorEnabled()) {
            logger.error("Error in method: {}. Error: {}",
                    joinPoint.getSignature().toShortString(), exception.getMessage());
        }
    }
}