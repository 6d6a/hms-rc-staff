package ru.majordomo.hms.rc.staff.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.repositories.ServiceTypeRepository;
import ru.majordomo.hms.rc.staff.resources.ServiceType;

import java.util.List;

@Service
public class GovernorOfServiceType {
    private ServiceTypeRepository serviceTypeRepository;

    @Autowired
    public void setRepository(ServiceTypeRepository repository) {
        this.serviceTypeRepository = repository;
    }

    public ServiceType build(String serviceTypeName) throws ResourceNotFoundException {
        ServiceType serviceType = serviceTypeRepository.findByName(serviceTypeName);
        if (serviceType == null) {
            throw new ResourceNotFoundException("ServiceType с именем:" + serviceTypeName + " не найден");
        }
        return serviceType;
    }

    public List<ServiceType> buildAll() {
        return serviceTypeRepository.findAll();
    }

    public void isValid(ServiceType serviceType) {
        for (ServiceType existedServiceType: serviceTypeRepository.findAll()) {
            if (existedServiceType.getName().equals(serviceType.getName())) {
                throw new ParameterValidateException("ServiceType c именем: " + serviceType.getName() + " уже существует");
            }
        }
    }

    public void save(ServiceType serviceType) {
        isValid(serviceType);
        serviceTypeRepository.save(serviceType);
    }

    public void delete(String name) {
        serviceTypeRepository.deleteServiceTypeByName(name);
    }
}
