package ru.majordomo.hms.rc.staff.managers;

import org.springframework.beans.factory.annotation.Autowired;

import ru.majordomo.hms.rc.staff.Resource;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

public class GovernorOfServiceTemplate {
    @Autowired
    ServiceTemplateRepository serviceTemplateRepository;

    public ServiceTemplate create() {
        return new ServiceTemplate();
    }
}
