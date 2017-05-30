package ru.majordomo.hms.rc.staff.resources.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import ru.majordomo.hms.rc.staff.resources.validation.validator.ServiceSocketValidator;

@Documented
@Constraint(validatedBy = ServiceSocketValidator.class)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidServiceSocket {
    String message() default "{ru.majordomo.hms.rc.staff.resources.validation.ValidServiceSocket.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String value() default "";
}
