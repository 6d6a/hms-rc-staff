package ru.majordomo.hms.rc.staff.resources.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ru.majordomo.hms.rc.staff.resources.Storage;
import ru.majordomo.hms.rc.staff.resources.validation.ValidStorage;

public class StorageValidator implements ConstraintValidator<ValidStorage, Storage> {

    @Override
    public void initialize(ValidStorage validStorage) {
    }

    @Override
    public boolean isValid(final Storage storage, ConstraintValidatorContext constraintValidatorContext) {
        boolean isValid = true;

        try {
            Double capacity = storage.getCapacity();
            Double capacityUsed = storage.getCapacityUsed();
            if (capacityUsed > capacity) {
                isValid = false;
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate("capacityUsed не может быть больше capacity")
                        .addConstraintViolation();
            }
        } catch (RuntimeException e) {
            System.out.println(e.toString());
            isValid = false;
        }

        return isValid;
    }
}
