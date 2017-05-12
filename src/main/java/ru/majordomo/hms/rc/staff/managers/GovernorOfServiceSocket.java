package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceSocketRepository;
import ru.majordomo.hms.rc.staff.resources.Network;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;
import ru.majordomo.hms.rc.staff.resources.validation.group.ServiceSocketChecks;

@Service
public class GovernorOfServiceSocket extends LordOfResources<ServiceSocket> {
    private Cleaner cleaner;
    private ServiceSocketRepository serviceSocketRepository;
    private GovernorOfService governorOfService;
    private Validator validator;

    @Autowired
    public void setCleaner(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    @Autowired
    public void setServiceSocketRepository(ServiceSocketRepository serviceSocketRepository) {
        this.serviceSocketRepository = serviceSocketRepository;
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
    public ServiceSocket createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        ServiceSocket serviceSocket = new ServiceSocket();
        LordOfResources.setResourceParams(serviceSocket, serviceMessage, cleaner);

        try {
            String address = cleaner.cleanString((String) serviceMessage.getParam("address"));
            Integer port = (Integer) serviceMessage.getParam("port");
            serviceSocket.setAddress(address);
            serviceSocket.setPort(port);
            isValid(serviceSocket);
            save(serviceSocket);
        } catch (ClassCastException e) {
            throw new ParameterValidateException("один из параметров указан неверно:" + e.getMessage());
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
    public ServiceSocket build(String resourceId) throws ResourceNotFoundException {
        ServiceSocket serviceSocket = serviceSocketRepository.findOne(resourceId);
        if (serviceSocket == null) {
            throw new ResourceNotFoundException("ServiceSocket с ID:" + resourceId + " не найден");
        }
        return serviceSocket;
    }

    @Override
    public ServiceSocket build(Map<String, String> keyValue) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public List<ServiceSocket> buildAll(Map<String, String> keyValue) {

        List<ServiceSocket> buildedServiceSockets = new ArrayList<>();

        Boolean byName = false;

        for (Map.Entry<String, String> entry : keyValue.entrySet()) {
            if (entry.getKey().equals("name")) {
                byName = true;
            }
        }

        if (byName) {
            for (ServiceSocket serviceSocket : serviceSocketRepository.findByName(keyValue.get("name"))) {
                buildedServiceSockets.add(build(serviceSocket.getId()));
            }
        } else {
            for (ServiceSocket serviceSocket : serviceSocketRepository.findAll()) {
                buildedServiceSockets.add(build(serviceSocket.getId()));
            }
        }

        return buildedServiceSockets;
    }

    @Override
    public List<ServiceSocket> buildAll() {
        return serviceSocketRepository.findAll();
    }

    @Override
    public void save(ServiceSocket resource) {
        serviceSocketRepository.save(resource);
    }

    @Override
    public void preDelete(String resourceId) {
        List<ru.majordomo.hms.rc.staff.resources.Service> services = governorOfService.buildAll();
        for (ru.majordomo.hms.rc.staff.resources.Service service : services) {
            if (service.getServiceSocketIds().contains(resourceId)) {
                throw new ParameterValidateException("Я нашла Service с ID " + service.getId()
                        + ", именуемый " + service.getName() + ", так вот в нём имеется удаляемый ServiceSocket");
            }
        }
    }

    @Override
    public void delete(String resourceId) {
        preDelete(resourceId);
        serviceSocketRepository.delete(resourceId);
    }

}
