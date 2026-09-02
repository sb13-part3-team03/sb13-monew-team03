package com.codeit.monew.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";
    private static final String CLIENT_IP = "clientIp";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString();
        String clientIp = getClientIp(request);

        try {
            // MDC에 저장
            MDC.put(REQUEST_ID, requestId);
            MDC.put(CLIENT_IP, clientIp);

            // 응답 헤더에 추가
            response.setHeader("X-Request-ID", requestId);
            response.setHeader("X-Client-IP", clientIp);

            filterChain.doFilter(request, response);
        } finally {
            // 스레드 재사용 시 이전 요청 정보가 남지 않도록 제거
            MDC.clear();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
