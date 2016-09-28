package ru.majordomo.hms.rc.staff.api.http;
//TODO

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.resources.Service;

@RestController
@RequestMapping("/${spring.application.name}/service")
public class ServiceRestController {

    private ServiceRepository repository;

    @Autowired
    public void setRepository(ServiceRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(value = "/{serviceId}", method = RequestMethod.GET)
    public Service readOne(@PathVariable String serviceId) {
        return repository.findOne(serviceId);
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<Service> readAll() {
        return repository.findAll();
    }
}
