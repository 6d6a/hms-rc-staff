package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@RestController
public class ServiceTemplateRestController extends RestControllerTemplate<ServiceTemplate> {

    @Autowired
    public void setGovernor(GovernorOfServiceTemplate governor) {
        this.governor = governor;
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_VIEW')")
    @RequestMapping(value = "/service-template/{serviceTemplateId}", method = RequestMethod.GET)
    public ServiceTemplate readOne(@PathVariable String serviceTemplateId) {
        return processReadOneQuery(serviceTemplateId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_VIEW')")
    @RequestMapping(value = "/service-template", method = RequestMethod.GET)
    public Collection<ServiceTemplate> readAll(
            @RequestParam(required=false, defaultValue="") String name,
            @RequestParam(required=false, defaultValue="") String regex
    ) {
        Map<String, String> keyValue = new HashMap<>();
        if (!name.isEmpty()) {
            keyValue.put("name", name);
        }
        if (!regex.isEmpty()) {
            keyValue.put("regex", "true");
        }
        if (!keyValue.isEmpty()) {
            return processReadAllWithParamsQuery(keyValue);
        } else {
            return processReadAllQuery();
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_VIEW')")
    @RequestMapping(value = "/service-template", method = RequestMethod.GET, headers = "X-HMS-Pageable=true")
    public Page<ServiceTemplate> readAll(
            @RequestParam(required=false, defaultValue="") String name,
            @RequestParam(required=false, defaultValue="") String regex,
            Pageable pageable
    ) {
        Map<String, String> keyValue = new HashMap<>();
        if (!name.isEmpty()) {
            keyValue.put("name", name);
        }
        if (!regex.isEmpty()) {
            keyValue.put("regex", "true");
        }
        if (!keyValue.isEmpty()) {
            return processReadAllWithParamsQuery(keyValue, pageable);
        } else {
            return processReadAllQuery(pageable);
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_VIEW')")
    @RequestMapping(value = "/service-template", method = RequestMethod.GET, headers = "X-HMS-Projection=OnlyIdAndName")
    public Collection<ServiceTemplate> readAll() {
        return processReadAllQueryOnlyIdAndName();
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_CREATE')")
    @RequestMapping(value = "/service-template", method = RequestMethod.POST)
    public ResponseEntity<ServiceTemplate> create(@RequestBody ServiceTemplate serviceTemplate) throws ParameterValidateException {
        return processCreateQuery(serviceTemplate);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_EDIT')")
    @RequestMapping(value = "/service-template/{serviceTemplateId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ServiceTemplate> update(@PathVariable String serviceTemplateId,
                                    @RequestBody ServiceTemplate serviceTemplate) throws ParameterValidateException {
        return processUpdateQuery(serviceTemplateId, serviceTemplate);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_DELETE')")
    @RequestMapping(value = "/service-template/{serviceTemplateId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable String serviceTemplateId) {
        return processDeleteQuery(serviceTemplateId);
    }
}
