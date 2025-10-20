package com.ulog.backend.compliance.aspect;

import com.ulog.backend.compliance.annotation.LogOperation;
import com.ulog.backend.compliance.service.OperationLogService;
import com.ulog.backend.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final OperationLogService operationLogService;

    public OperationLogAspect(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Around("@annotation(com.ulog.backend.compliance.annotation.LogOperation)")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        LogOperation logAnnotation = signature.getMethod().getAnnotation(LogOperation.class);
        
        String operationType = logAnnotation.value();
        String description = logAnnotation.description();
        
        Long userId = getCurrentUserId();
        HttpServletRequest request = getCurrentRequest();
        
        Object result = null;
        Integer statusCode = 200;
        String errorMessage = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            statusCode = 500;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            String operationDetail = buildOperationDetail(description, duration, joinPoint);
            
            if (errorMessage != null) {
                operationLogService.logOperationWithError(userId, operationType, 
                    operationDetail, errorMessage, statusCode);
            } else {
                operationLogService.logOperation(userId, operationType, 
                    operationDetail, request, statusCode);
            }
        }
    }

    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                return userPrincipal.getUserId();
            }
        } catch (Exception e) {
            log.debug("Could not get current user ID", e);
        }
        return null;
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String buildOperationDetail(String description, long duration, ProceedingJoinPoint joinPoint) {
        StringBuilder detail = new StringBuilder();
        
        if (description != null && !description.isEmpty()) {
            detail.append(description);
        }
        
        detail.append(" | Method: ").append(joinPoint.getSignature().getName());
        detail.append(" | Duration: ").append(duration).append("ms");
        
        return detail.toString();
    }
}

