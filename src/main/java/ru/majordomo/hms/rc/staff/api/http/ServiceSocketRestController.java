package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import ru.majordomo.hms.rc.staff.repositories.ServiceSocketRepository;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;

@RestController
@CrossOrigin("*")
@RequestMapping("/${spring.application.name}/service-socket")
public class ServiceSocketRestController {

    private ServiceSocketRepository serviceSocketRepository;

    @Autowired
    public void setServiceSocketRepository(ServiceSocketRepository serviceSocketRepository) {
        this.serviceSocketRepository = serviceSocketRepository;
    }

    @RequestMapping(value = "/{serviceSocketId}", method = RequestMethod.GET)
    public ServiceSocket readOne(@PathVariable String serviceSocketId) {
        return serviceSocketRepository.findOne(serviceSocketId);
    }

    @RequestMapping(value = {"","/"}, method = RequestMethod.GET)
    public Collection<ServiceSocket> findAll() {
        return serviceSocketRepository.findAll();
    }
}
