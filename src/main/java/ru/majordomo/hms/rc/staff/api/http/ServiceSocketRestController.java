package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
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
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceSocket;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;

@RestController
@RequestMapping("/service-socket")
public class ServiceSocketRestController extends RestControllerTemplate<ServiceSocket> {

    @Autowired
    public void setGovernor(GovernorOfServiceSocket governor) {
        this.governor = governor;
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_VIEW')")
    @RequestMapping(value = "/{serviceSocketId}", method = RequestMethod.GET)
    public ServiceSocket readOne(@PathVariable String serviceSocketId) {
        return processReadOneQuery(serviceSocketId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_VIEW')")
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<ServiceSocket> readAll(@RequestParam(required=false, defaultValue="") String name) {
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

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_CREATE')")
    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<ServiceSocket> create(@RequestBody ServiceSocket socket) throws ParameterValidateException {
        return processCreateQuery(socket);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_EDIT')")
    @RequestMapping(value = "/{serviceSocketId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ServiceSocket> update(@PathVariable String serviceSocketId, @RequestBody ServiceSocket socket) throws ParameterValidateException {
        return processUpdateQuery(serviceSocketId, socket);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_DELETE')")
    @RequestMapping(value = "/{serviceSocketId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable String serviceSocketId) {
        return processDeleteQuery(serviceSocketId);
    }
}
