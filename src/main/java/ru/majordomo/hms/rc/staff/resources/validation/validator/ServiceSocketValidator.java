package ru.majordomo.hms.rc.staff.resources.validation.validator;


import com.google.common.net.InetAddresses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ru.majordomo.hms.rc.staff.resources.Network;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;
import ru.majordomo.hms.rc.staff.resources.validation.ValidServiceSocket;

public class ServiceSocketValidator implements ConstraintValidator<ValidServiceSocket, ServiceSocket> {
    private final MongoOperations operations;

    @Autowired
    public ServiceSocketValidator(MongoOperations operations) {
        this.operations = operations;
    }

    @Override
    public void initialize(ValidServiceSocket validServiceSocket) {
    }

    @Override
    public boolean isValid(final ServiceSocket serviceSocket, ConstraintValidatorContext constraintValidatorContext) {
        boolean isValid = false;

        try {
            String address = serviceSocket.getAddressAsString();
            if (address.equals("") || !InetAddresses.isInetAddress(address)) {
                isValid = false;
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate("параметр address указан неверно")
                        .addConstraintViolation();
            }

            List<Network> networkList = operations.findAll(Network.class);

            for (Network network : networkList) {
                if (network.isAddressIn(address)) {
                    isValid = true;
                    break;
                }
            }
            if (!isValid) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate("Адрес: " + address + " не принадлежит ни к одной известной сети")
                        .addConstraintViolation();
            }
        } catch (RuntimeException e) {
            System.out.println(e.toString());
            isValid = false;
        }

        return isValid;
    }
}
