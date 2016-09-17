package ru.majordomo.hms.rc.staff.managers;

import ru.majordomo.hms.rc.staff.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;

public abstract class LordOfResources {
    public abstract Resource createResource(ServiceMessage serviceMessage) throws ParameterValidateException;
}
