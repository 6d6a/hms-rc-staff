package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceType;
import ru.majordomo.hms.rc.staff.resources.ServiceType;

import java.util.Collection;

@RestController
@RequestMapping("/service-type")
public class ServiceTypeRestController {

    private GovernorOfServiceType governorOfServiceType;

    @Autowired
    public void setGovernor(GovernorOfServiceType governor) {
        this.governorOfServiceType = governor;
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TYPE_VIEW')")
    @RequestMapping(value = "/{serviceTypeName}", method = RequestMethod.GET)
    public ServiceType readOne(@PathVariable String serviceTypeName) {
        return governorOfServiceType.build(serviceTypeName);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TYPE_VIEW')")
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<ServiceType> readAll() {
        return governorOfServiceType.buildAll();
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TYPE_CREATE')")
    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<?> create (@RequestBody ServiceType serviceType) throws ParameterValidateException {
        governorOfServiceType.validateAndStore(serviceType);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{serviceTypeName}")
                .buildAndExpand(serviceType.getName()).toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TYPE_DELETE')")
    @RequestMapping(value = "/{serviceTypeName}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable String serviceTypeName) {
        ServiceType storedServiceType = governorOfServiceType.build(serviceTypeName);
        governorOfServiceType.delete(storedServiceType.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
