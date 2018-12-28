package ru.majordomo.hms.rc.staff.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Don't use with JsonView for one endpoint because JsonViewResponseBodyAdvice set view after custom advice
 * @see com.fasterxml.jackson.annotation.JsonView
 * @see org.springframework.web.servlet.mvc.method.annotation.JsonViewResponseBodyAdvice
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityView {

    /**
     * Views with authorities in order as is
     */
    View[] value();

    /**
     * Fallback view will be used if not matching with views authorities
     * Fallback view's authorities ignored
     */
    View fallback();

    @interface View {
        /**
         * Class of the view
         */
        Class<?> value() default Object.class;

        /**
         * If showAll is true advice use value as null
         */
        boolean showAll() default false;

        /**
         * Array of roles and privileges
         */
        String[] authorities() default {};
    }
}
