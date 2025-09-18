package dev.mkopp.mysite.shared.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {

    @Value("${application.logging.service.log-request-args:false}")
    private boolean logRequestArgs;

    @Pointcut("@within(org.springframework.stereotype.Service)")
    public void serviceLayer() {}

    @Before("serviceLayer()")
    public void logCall(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        if (logRequestArgs) {
            log.debug("Service Call: {}.{}({})",
                    signature.getDeclaringType().getSimpleName(),
                    signature.getName(),
                    joinPoint.getArgs());
        } else {
            log.debug("Service Call: {}.{}",
                    signature.getDeclaringType().getSimpleName(),
                    signature.getName());
        }
    }

    @AfterReturning(pointcut = "serviceLayer()", returning = "result")
    public void logResponse(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.debug("Service Response: {}.{} -> {}",
                signature.getDeclaringType().getSimpleName(),
                signature.getName(),
                result);
    }

    @AfterThrowing(pointcut = "serviceLayer()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.error("Exception in {}.{}",
                signature.getDeclaringType().getSimpleName(),
                signature.getName(),
                exception);
    }
}