package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ServiceSocketRepository;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;
import ru.majordomo.hms.rc.staff.resources.validation.group.ServiceSocketChecks;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GovernorOfServiceSocket extends LordOfResources<ServiceSocket> {
    private Cleaner cleaner;
    private GovernorOfService governorOfService;
    private Validator validator;

    @Autowired
    public void setCleaner(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    @Autowired
    public void setServiceSocketRepository(ServiceSocketRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setGovernorOfService(GovernorOfService governorOfService) {
        this.governorOfService = governorOfService;
    }

    @Autowired
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    public ServiceSocket buildResourceFromServiceMessage(ServiceMessage serviceMessage) throws ClassCastException, UnsupportedEncodingException {
        ServiceSocket serviceSocket = new ServiceSocket();

        try {
            LordOfResources.setResourceParams(serviceSocket, serviceMessage, cleaner);
            String address = cleaner.cleanString((String) serviceMessage.getParam("address"));
            Integer port = (Integer) serviceMessage.getParam("port");
            serviceSocket.setAddress(address);
            serviceSocket.setPort(port);
        } catch (ClassCastException e) {
            throw new ParameterValidateException("???????? ???? ???????????????????? ???????????? ??????????????:" + e.getMessage());
        }

        return serviceSocket;
    }

    @Override
    public void isValid(ServiceSocket serviceSocket) throws ParameterValidateException {
        Set<ConstraintViolation<ServiceSocket>> constraintViolations = validator.validate(serviceSocket, ServiceSocketChecks.class);

        if (!constraintViolations.isEmpty()) {
            logger.error("serviceSocket: " + serviceSocket + " constraintViolations: " + constraintViolations.toString());
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    @Override
    public ServiceSocket build(Map<String, String> keyValue) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public void preDelete(String resourceId) {
        List<ru.majordomo.hms.rc.staff.resources.Service> services = governorOfService.buildAll();
        for (ru.majordomo.hms.rc.staff.resources.Service service : services) {
            if (service.getServiceSocketIds().contains(resourceId)) {
                throw new ParameterValidateException("?? ?????????? Service ?? ID " + service.getId()
                        + ", ?????????????????? " + service.getName() + ", ?????? ?????? ?? ?????? ?????????????? ?????????????????? ServiceSocket");
            }
        }
    }
}
