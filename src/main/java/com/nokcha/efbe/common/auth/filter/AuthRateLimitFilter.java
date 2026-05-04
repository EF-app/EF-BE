//package com.nokcha.efbe.common.auth.filter;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.ArrayDeque;
//import java.util.Deque;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//public class AuthRateLimitFilter extends OncePerRequestFilter {     // 단기간 로그인 방지를 위한 클래스
//
//    private static final Duration WINDOW = Duration.ofMinutes(1);
//    private static final int LOGIN_LIMIT = 10;
//    private static final int SIGNUP_LIMIT = 5;
//    private static final int ADMIN_LOGIN_LIMIT = 5;
//
//    private final Map<String, Deque<Instant>> buckets = new ConcurrentHashMap<>();
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
//        String uri = request.getRequestURI();
//        Integer limit = resolveLimit(uri);
//        if (limit == null) {
//            chain.doFilter(request, response);
//            return;
//        }
//
//        String key = clientIp(request) + ":" + bucketKey(uri);
//        if (!allow(key, limit)) {
//            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
//            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//            response.setCharacterEncoding("UTF-8");
//            response.getWriter().write("{\"code\":429,\"status\":\"TOO_MANY_REQUESTS\",\"errorMessage\":\"요청이 너무 많습니다. 잠시 후 다시 시도해주세요.\"}");
//            return;
//        }
//        chain.doFilter(request, response);
//    }
//
//    private Integer resolveLimit(String uri) {
//        if (uri.equals("/v1/users/login")) return LOGIN_LIMIT;
//        if (uri.startsWith("/v1/users/signup")) return SIGNUP_LIMIT;
//        if (uri.equals("/v1/admin/auth/login")) return ADMIN_LOGIN_LIMIT;
//        return null;
//    }
//
//    private String bucketKey(String uri) {
//        if (uri.startsWith("/v1/users/signup")) return "signup";
//        if (uri.equals("/v1/admin/auth/login")) return "admin-login";
//        return "login";
//    }
//
//    // X-Forwarded-For 우선 (프록시 뒤 운영 환경 대비), 없으면 RemoteAddr
//    private String clientIp(HttpServletRequest request) {
//        String xff = request.getHeader("X-Forwarded-For");
//        if (xff != null && !xff.isBlank()) {
//            int comma = xff.indexOf(',');
//            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
//        }
//        return request.getRemoteAddr();
//    }
//
//    private boolean allow(String key, int limit) {
//        Instant now = Instant.now();
//        Instant cutoff = now.minus(WINDOW);
//        Deque<Instant> stamps = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());
//        synchronized (stamps) {
//            while (!stamps.isEmpty() && stamps.peekFirst().isBefore(cutoff)) {
//                stamps.pollFirst();
//            }
//            if (stamps.size() >= limit) return false;
//            stamps.addLast(now);
//            return true;
//        }
//    }
//}
