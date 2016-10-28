package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;

import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceSocket;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;

@RestController
@CrossOrigin("*")
@RequestMapping("/${spring.application.name}/service-socket")
public class ServiceSocketRestController {

    private GovernorOfServiceSocket governor;
    private String applicationName;

    @Value("${spring.application.name}")
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Autowired
    public void setGovernor(GovernorOfServiceSocket governor) {
        this.governor = governor;
    }

    @RequestMapping(value = "/{serviceSocketId}", method = RequestMethod.GET)
    public ServiceSocket readOne(@PathVariable String serviceSocketId) {
        return (ServiceSocket) governor.build(serviceSocketId);
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<ServiceSocket> findAll() {
        return governor.build();
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody ServiceSocket socket) throws ParameterValidateException {
        governor.isValid(socket);
        governor.save(socket);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(socket.getId()).toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{serviceSocketId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<?> update(@PathVariable String serviceSocketId, @RequestBody ServiceSocket socket) throws ParameterValidateException {
        governor.isValid(socket);
        ServiceSocket storedSocket = (ServiceSocket) governor.build(serviceSocketId);
        socket.setId(storedSocket.getId());
        governor.save(socket);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{serviceSocketId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable String serviceSocketId) {
        ServiceSocket storedSocket = (ServiceSocket) governor.build(serviceSocketId);
        governor.delete(serviceSocketId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
