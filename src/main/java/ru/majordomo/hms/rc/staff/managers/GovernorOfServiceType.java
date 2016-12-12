package ru.majordomo.hms.rc.staff.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.repositories.ServiceTypeRepository;
import ru.majordomo.hms.rc.staff.resources.ServiceType;
import java.util.regex.*;

import java.util.List;

@Service
public class GovernorOfServiceType {
    private ServiceTypeRepository serviceTypeRepository;

    @Autowired
    public void setRepository(ServiceTypeRepository repository) {
        this.serviceTypeRepository = repository;
    }

    public ServiceType build(String serviceTypeName) throws ResourceNotFoundException {
        ServiceType serviceType = serviceTypeRepository.findByName(serviceTypeName.toUpperCase());
        if (serviceType == null) {
            throw new ResourceNotFoundException("ServiceType с именем:" + serviceTypeName + " не найден");
        }
        return serviceType;
    }

    public List<ServiceType> buildAll() {
        return serviceTypeRepository.findAll();
    }

    public void isValid(ServiceType serviceType) {
        validateName(serviceType.getName());
        for (ServiceType existedServiceType: serviceTypeRepository.findAll()) {
            if (existedServiceType.getName().equals(serviceType.getName())) {
                throw new ParameterValidateException("ServiceType c именем: " + serviceType.getName() + " уже существует");
            }
        }
    }

    public void save(ServiceType serviceType) {
        serviceType.setName(serviceType.getName().toUpperCase());
        isValid(serviceType);
        serviceTypeRepository.save(serviceType);
    }

    private void validateName(String name) {

        String patternDatabase = "DATABASE_[A-Z]+";
        Pattern pattern = Pattern.compile(patternDatabase, Pattern.CASE_INSENSITIVE);
        Matcher matcherDatabase = pattern.matcher(name);
        boolean isMatchedDatabase = matcherDatabase.matches();

        String patternWebsite = "WEBSITE_[A-Z0-9]+_[A-Z0-9]+_[A-Z0-9]+";
        Pattern pattern1 = Pattern.compile(patternWebsite, Pattern.CASE_INSENSITIVE);
        Matcher matcherWebsite = pattern1.matcher(name);
        boolean isMatchedWebsite = matcherWebsite.matches();

        String patternMailStorage = "MAILBOX_[A-Z]+";
        Pattern pattern2 = Pattern.compile(patternMailStorage, Pattern.CASE_INSENSITIVE);
        Matcher matcherMailStorage = pattern2.matcher(name);
        boolean isMatchedMailStorage = matcherMailStorage.matches();

        if (!isMatchedDatabase && !isMatchedWebsite && !isMatchedMailStorage) {
            throw new ParameterValidateException("Заданное имя: " + name + " некорректно");
        }
    }

    public void delete(String name) {
        serviceTypeRepository.deleteServiceTypeByName(name);
    }
}
