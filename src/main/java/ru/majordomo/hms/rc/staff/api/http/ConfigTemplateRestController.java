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
import ru.majordomo.hms.rc.staff.managers.GovernorOfConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;

@RestController
@RequestMapping("/config-template")
public class ConfigTemplateRestController extends RestControllerTemplate<ConfigTemplate> {

    @Autowired
    public void setGovernor(GovernorOfConfigTemplate governor) {
        this.governor = governor;
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('CONFIG_TEMPLATE_VIEW')")
    @RequestMapping(value = {"/{configTemplateId}", "/{configTemplateId}/"}, method = RequestMethod.GET)
    public ConfigTemplate readOne(@PathVariable String configTemplateId) {
        return processReadOneQuery(configTemplateId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('CONFIG_TEMPLATE_VIEW')")
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<ConfigTemplate> readAll(@RequestParam(required=false, defaultValue="") String name) {
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

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('CONFIG_TEMPLATE_CREATE')")
    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<ConfigTemplate> create(@RequestBody ConfigTemplate configTemplate) throws ParameterValidateException {
        return processCreateQuery(configTemplate);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMIN') or hasAuthority('CONFIG_TEMPLATE_EDIT')")
    @RequestMapping(value = {"/{configTemplateId}", "/{configTemplateId}/"}, method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ConfigTemplate> update(@PathVariable String configTemplateId,
                                    @RequestBody ConfigTemplate configTemplate)
                                    throws ParameterValidateException {
        return processUpdateQuery(configTemplateId, configTemplate);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('CONFIG_TEMPLATE_DELETE')")
    @RequestMapping(value = {"/{configTemplateId}", "/{configTemplateId}/"}, method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable String configTemplateId) {
        return processDeleteQuery(configTemplateId);
    }
}
