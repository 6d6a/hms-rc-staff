package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfService;
import ru.majordomo.hms.rc.staff.resources.Service;

@RestController
public class ServiceRestController extends RestControllerTemplate<Service> {

    @Autowired
    public void setGovernor(GovernorOfService governor) {
        this.governor = governor;
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_VIEW')")
    @RequestMapping(value = "/service/{serviceId}", method = RequestMethod.GET)
    public Service readOne(@PathVariable String serviceId) {
        return processReadOneQuery(serviceId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_VIEW')")
    @RequestMapping(value = {"/service", "/service/"}, method = RequestMethod.GET)
    public Collection<Service> readAll(@RequestParam(required=false, defaultValue="") String name) {
        Map<String, String> keyValue = new HashMap<>();
        if (!name.isEmpty()) {
            keyValue.put("name", name);
        }
        if (!keyValue.isEmpty()) {
            return processReadAllWithParamsQuery(keyValue);
        } else {
            return processReadAllQuery();
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_VIEW')")
    @RequestMapping(value = {"/service", "/service/"}, method = RequestMethod.GET, headers = "X-HMS-Pageable=true")
    public Page<Service> readAll(
            @RequestParam(required=false, defaultValue="") String name,
            Pageable pageable
    ) {
        Map<String, String> keyValue = new HashMap<>();
        if (!name.isEmpty()) {
            keyValue.put("name", name);
        }
        if (!keyValue.isEmpty()) {
            return processReadAllWithParamsQuery(keyValue, pageable);
        } else {
            return processReadAllQuery(pageable);
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_CREATE')")
    @RequestMapping(value = {"/service", "/service/"}, method = RequestMethod.POST)
    public ResponseEntity<Service> create(@RequestBody Service service) throws ParameterValidateException {
        return processCreateQuery(service);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_EDIT')")
    @RequestMapping(value = "/service/{serviceId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<Service> update(@PathVariable String serviceId,
                                    @RequestBody Service service) throws ParameterValidateException {
        return processUpdateQuery(serviceId, service);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_DELETE')")
    @RequestMapping(value = "/service/{serviceId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable String serviceId) throws ParameterValidateException {
        return processDeleteQuery(serviceId);
    }

    //Возвращает список объектов Service
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @RequestMapping(value = "/server/{serverId}/services", method = RequestMethod.GET)
    public Collection<Service> readAllServicesByServerId(
            @PathVariable String serverId,
            @RequestParam Map<String,String> requestParams
    ) {
        requestParams.put("serverId", serverId);

        return processReadAllWithParamsQuery(requestParams);
    }
}
