package ru.majordomo.hms.rc.staff.managers;

import org.springframework.stereotype.Component;

import ru.majordomo.hms.rc.staff.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;

@Component
public class GovernorOfServer extends LordOfResources{

    @Override
    public Resource createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        return null;
    }

    @Override
    public void isValid(Resource resource) throws ParameterValidateException {

    }
}
