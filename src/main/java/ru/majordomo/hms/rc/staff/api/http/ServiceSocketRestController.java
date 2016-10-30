package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceSocket;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;

@RestController
@CrossOrigin("*")
@RequestMapping("/service-socket")
public class ServiceSocketRestController extends RestControllerTemplate {

    @Autowired
    public void setGovernor(GovernorOfServiceSocket governor) {
        this.governor = governor;
    }

    @RequestMapping(value = "/{serviceSocketId}", method = RequestMethod.GET)
    public ServiceSocket readOne(@PathVariable String serviceSocketId) {
        return (ServiceSocket) processReadOneQuery(serviceSocketId);
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<? extends Resource> readAll(@RequestParam(required=false, defaultValue="") String name) {
        return processReadAllQuery(name);
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody ServiceSocket socket) throws ParameterValidateException {
        return processCreateQuery(socket);
    }

    @RequestMapping(value = "/{serviceSocketId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<?> update(@PathVariable String serviceSocketId, @RequestBody ServiceSocket socket) throws ParameterValidateException {
        return processUpdateQuery(serviceSocketId, socket);
    }

    @RequestMapping(value = "/{serviceSocketId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable String serviceSocketId) {
        return processDeleteQuery(serviceSocketId);
    }
}
