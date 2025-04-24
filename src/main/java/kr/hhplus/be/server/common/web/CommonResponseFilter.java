package kr.hhplus.be.server.common.web;

import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.presentation.common.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonResponseFilter extends OncePerRequestFilter {

    private static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

    private final JsonMapper jsonMapper = new JsonMapper();

    private final ExceptionTranslator<BusinessException> exceptionTranslator;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException {
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        CommonResponse<Object> commonResponse;
        try {
            filterChain.doFilter(request, wrappedResponse);

            byte[] bytes = wrappedResponse.getContentAsByteArray();
            Object body = jsonMapper.readValue(bytes, Object.class);
            commonResponse = CommonResponse.success(body);

            jsonMapper.writeValue(response.getOutputStream(), commonResponse);
        } catch (Throwable t) {
            log.info(t.getMessage(), t);

            BusinessException translated = exceptionTranslator.translate(t);
            BusinessError businessError = translated.getBusinessError();
            commonResponse = CommonResponse.fail(businessError.getStatus(), businessError.getMessage());

            response.setStatus(businessError.getStatus().value());
            response.setContentType(APPLICATION_JSON_UTF8.toString());
        }

        jsonMapper.writeValue(response.getOutputStream(), commonResponse);
    }

}
