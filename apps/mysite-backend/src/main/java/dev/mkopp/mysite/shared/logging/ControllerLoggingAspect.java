package dev.mkopp.mysite.shared.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Component
public class ControllerLoggingAspect {

    @Value("${application.logging.controller.log-request-args:false}")
    private boolean logRequestArgs;

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restController() {}

    @Before("restController()")
    public void logRequest(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (logRequestArgs) {
            log.debug("HTTP {} Request: {}.{}({})",
                    request.getMethod(),
                    signature.getDeclaringType().getSimpleName(),
                    signature.getName(),
                    joinPoint.getArgs());
        } else {
            log.debug("HTTP {} Request: {}.{}",
                    request.getMethod(),
                    signature.getDeclaringType().getSimpleName(),
                    signature.getName());
        }
    }

    @AfterReturning(pointcut = "restController()", returning = "result")
    public void logResponse(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.debug("HTTP Response: {}.{} -> {}",
                signature.getDeclaringType().getSimpleName(),
                signature.getName(),
                result);
    }

    @AfterThrowing(pointcut = "restController()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        log.error("Exception in {}.{}",
                signature.getDeclaringType().getSimpleName(),
                signature.getName(),
                exception);
    }
}