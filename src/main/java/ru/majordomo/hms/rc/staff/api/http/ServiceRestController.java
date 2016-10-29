package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfService;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.resources.Service;

@RestController
@RequestMapping("/service")
public class ServiceRestController extends RestControllerTemplate {

    @Autowired
    public void setGovernor(GovernorOfService governor) {
        this.governor = governor;
    }

    @RequestMapping(value = "/{serviceId}", method = RequestMethod.GET)
    public Service readOne(@PathVariable String serviceId) {
        return (Service) processReadOneQuery(serviceId);
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<? extends Resource> readAll() {
        return processReadAllQuery();
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody Service service) throws ParameterValidateException {
        return processCreateQuery(service);
    }

    @RequestMapping(value = "/{serviceId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<?> update(@PathVariable String serviceId,
                                    @RequestBody Service service) throws ParameterValidateException {
        return processUpdateQuery(serviceId, service);
    }

    @RequestMapping(value = "/{serviceId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable String serviceId) throws ParameterValidateException {
        return processDeleteQuery(serviceId);
    }
}
