package ru.majordomo.hms.rc.staff.resources.validation.validator;


import com.google.common.net.InetAddresses;

import org.apache.commons.net.util.SubnetUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.resources.Network;
import ru.majordomo.hms.rc.staff.resources.validation.ValidNetwork;

public class NetworkValidator implements ConstraintValidator<ValidNetwork, Network> {

    @Override
    public void initialize(ValidNetwork validNetwork) {
    }

    @Override
    public boolean isValid(final Network network, ConstraintValidatorContext constraintValidatorContext) {
        boolean isValid = true;

        try {
            String address = network.getAddressAsString();
            if (address.equals("") || !InetAddresses.isInetAddress(address)) {
                isValid = false;
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate("параметр address указан неверно")
                        .addConstraintViolation();
            }

            String gwAddress = network.getGatewayAddressAsString();
            if (gwAddress.equals("") || !InetAddresses.isInetAddress(gwAddress)) {
                isValid = false;
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate("параметр gatewayAddress указан неверно")
                        .addConstraintViolation();
            }

            Integer netmask = network.getMask();

            SubnetUtils subnetUtils = new SubnetUtils(address + "/" + netmask);
            SubnetUtils.SubnetInfo subnetInfo = subnetUtils.getInfo();
            if (!subnetInfo.isInRange(gwAddress)) {
                isValid = false;
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(gwAddress + " не входит в сеть " + address + "/" + netmask.toString())
                        .addConstraintViolation();
            }
        } catch (RuntimeException e) {
            System.out.println(e.toString());
            isValid = false;
        }

        return isValid;
    }
}
