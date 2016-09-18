package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@RestController
@CrossOrigin
@RequestMapping("/rc/service-template")
public class ServiceTemplateRestController {
    @Autowired
    ServiceTemplateRepository serviceTemplateRepository;

    @RequestMapping(value = "{serviceTemplateId}", method = RequestMethod.GET)
    public ServiceTemplate readOne(@PathVariable String serviceTemplateId) {
        return serviceTemplateRepository.findOne(serviceTemplateId);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Collection<ServiceTemplate> readAll() {
        return serviceTemplateRepository.findAll();
    }
}
