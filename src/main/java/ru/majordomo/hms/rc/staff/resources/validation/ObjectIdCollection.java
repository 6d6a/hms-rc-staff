package ru.majordomo.hms.rc.staff.resources.validation;

import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.resources.validation.validator.ObjectIdCollectionValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ObjectIdCollectionValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectIdCollection {
    String message() default "{ru.majordomo.hms.rc.staff.resources.validation.ObjectIdCollection.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends Resource> value();

    String collection() default "";
}
