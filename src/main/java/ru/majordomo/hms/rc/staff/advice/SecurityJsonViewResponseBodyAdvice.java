package ru.majordomo.hms.rc.staff.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMappingJacksonResponseBodyAdvice;
import ru.majordomo.hms.rc.staff.annotation.SecurityView;
import ru.majordomo.hms.rc.staff.annotation.SecurityView.View;

import java.security.Principal;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@ControllerAdvice
public class SecurityJsonViewResponseBodyAdvice extends AbstractMappingJacksonResponseBodyAdvice {
    private static final Logger log = LoggerFactory.getLogger(SecurityJsonViewResponseBodyAdvice.class);

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return super.supports(returnType, converterType) && returnType.hasMethodAnnotation(SecurityView.class);
    }

    @Override
    protected void beforeBodyWriteInternal(
            MappingJacksonValue bodyContainer,
            MediaType contentType,
            MethodParameter returnType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        Principal principal = request.getPrincipal();
        log.debug("username: {}, request {} {}", principal.getName(), request.getMethodValue(), request.getURI());

        if (principal instanceof Authentication && ((Authentication) principal).isAuthenticated()) {
            SecurityView annotation = returnType.getMethodAnnotation(SecurityView.class);

            if (annotation != null) {
                Class<?> viewClass = resolveView(annotation, principal);
                bodyContainer.setSerializationView(viewClass);
            }
        }
    }

    private Class<?> resolveView(SecurityView annotation, Principal principal) {
        Collection<? extends GrantedAuthority> grantedAuthorities = ((Authentication) principal).getAuthorities();

        if (grantedAuthorities != null) {
            Set<String> authorities = grantedAuthorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            for (View view : annotation.value()) {
                for (String authority : view.authorities()) {
                    if (authorities.contains(authority)) {
                        Class<?> viewClass = getViewClass(view);
                        log.debug("matches {} return view {}", authority, viewClass);
                        return viewClass;
                    }
                }
            }
        }
        Class<?> viewClass = getViewClass(annotation.fallback());
        log.debug("return fallback view {}", viewClass);
        return viewClass;
    }

    private Class<?> getViewClass(View view) {
        if (view.showAll()) {
            return null;
        } else {
            return view.value();
        }
    }
}
