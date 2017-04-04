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
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@RestController
@RequestMapping("/service-template")
public class ServiceTemplateRestController extends RestControllerTemplate<ServiceTemplate> {

    @Autowired
    public void setGovernor(GovernorOfServiceTemplate governor) {
        this.governor = governor;
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_VIEW')")
    @RequestMapping(value = "{serviceTemplateId}", method = RequestMethod.GET)
    public ServiceTemplate readOne(@PathVariable String serviceTemplateId) {
        return processReadOneQuery(serviceTemplateId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_VIEW')")
    @RequestMapping(value = {"/",""}, method = RequestMethod.GET)
    public Collection<ServiceTemplate> readAll(@RequestParam(required=false, defaultValue="") String name) {
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

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_CREATE')")
    @RequestMapping(value = {"", ""}, method = RequestMethod.POST)
    public ResponseEntity<ServiceTemplate> create(@RequestBody ServiceTemplate serviceTemplate) throws ParameterValidateException {
        return processCreateQuery(serviceTemplate);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_EDIT')")
    @RequestMapping(value = "/{serviceTemplateId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ServiceTemplate> update(@PathVariable String serviceTemplateId,
                                    @RequestBody ServiceTemplate serviceTemplate) throws ParameterValidateException {
        return processUpdateQuery(serviceTemplateId, serviceTemplate);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_DELETE')")
    @RequestMapping(value = "/{serviceTemplateId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable String serviceTemplateId) {
        return processDeleteQuery(serviceTemplateId);
    }

}
