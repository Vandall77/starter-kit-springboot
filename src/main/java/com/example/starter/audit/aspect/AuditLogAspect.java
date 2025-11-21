package com.example.starter.audit.aspect;

import com.example.starter.audit.entity.AuditLog;
import com.example.starter.audit.service.AuditLogService;
import com.example.starter.rbac.user.repository.UserAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;
    private final UserAccountRepository userAccountRepository;
    private final HttpServletRequest request;

    @Around("@annotation(auditable)")
    public Object around(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        Instant now = Instant.now();
        String action = auditable.action();

        // 🔑 username sekarang di-resolve lewat helper
        String username = resolveUsername(pjp);

        AuditLog log = new AuditLog();
        log.setEventTime(now);
        log.setAction(action);
        log.setStatus("SUCCESS");
        log.setUsername(username);
        log.setIpAddress(request.getRemoteAddr());
        log.setPath(request.getRequestURI());
        log.setHttpMethod(request.getMethod());

        if (username != null && !"anonymousUser".equals(username)) {
            userAccountRepository.findByUsernameAndDeletedAtIsNull(username)
                    .ifPresent(log::setUser);
        }

        try {
            Object result = pjp.proceed();
            auditLogService.log(log);
            return result;
        } catch (Throwable ex) {
            log.setStatus("FAILED");
            log.setMessage(ex.getMessage());
            auditLogService.log(log);
            throw ex;
        }
    }

    /**
     * Resolusi username:
     * 1. Kalau SecurityContext sudah authenticated dan bukan anonymous → pakai itu.
     * 2. Kalau masih anonymous (misalnya di /api/auth/login) → coba ambil dari parameter yang punya getUsername().
     * 3. Kalau tetap nggak ada → "anonymousUser".
     */
    private String resolveUsername(ProceedingJoinPoint pjp) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null
                && auth.isAuthenticated()
                && auth.getName() != null
                && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }

        // Fallback: cari argumen yang punya method getUsername()
        Object[] args = pjp.getArgs();
        if (args != null) {
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                try {
                    // Reflection: cek apakah ada method getUsername()
                    java.lang.reflect.Method m = arg.getClass().getMethod("getUsername");
                    Object val = m.invoke(arg);
                    if (val instanceof String) {
                        String uname = ((String) val).trim();
                        if (!uname.isEmpty()) {
                            return uname;
                        }
                    }
                } catch (NoSuchMethodException e) {
                    // arg ini tidak punya getUsername, skip
                } catch (Exception e) {
                    // error lain saat invoke, abaikan supaya tidak ganggu flow utama
                }
            }
        }

        return "anonymousUser";
    }
}
