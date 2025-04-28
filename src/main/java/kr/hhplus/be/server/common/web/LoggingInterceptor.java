package kr.hhplus.be.server.common.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Method;

@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String ATTRIBUTE_NAME = "x-requested-time";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Method method = handlerMethod.getMethod();
        Logged annotation = AnnotationUtils.findAnnotation(method, Logged.class);

        if (annotation == null) {
            return true;
        }

        long startTime = System.nanoTime();
        request.setAttribute(ATTRIBUTE_NAME, startTime);

        return true;
    }

    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView
    ) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return;
        }

        Object attribute = request.getAttribute(ATTRIBUTE_NAME);
        if (!(attribute instanceof Long startTime)) {
            return;
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // nano -> millisecond

        Method method = handlerMethod.getMethod();
        Logged annotation = AnnotationUtils.findAnnotation(method, Logged.class);
        if (annotation == null) {
            return;
        }

        if (annotation.threshold() >= duration) {
            return;
        }

        String httpMethod = request.getMethod();
        String uri = request.getRequestURI();

        log.info("{} {}: {} ms", httpMethod, uri, duration);
    }

}
