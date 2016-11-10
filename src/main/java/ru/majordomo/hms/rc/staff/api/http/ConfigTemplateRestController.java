package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import ru.majordomo.hms.rc.staff.resources.Resource;

@RestController
@RequestMapping("/config-template")
public class ConfigTemplateRestController extends TemplateRestController {

    @Autowired
    public void setGovernor(GovernorOfConfigTemplate governor) {
        this.governor = governor;
    }

    @RequestMapping(value = "/{configTemplateId}", method = RequestMethod.GET)
    public ConfigTemplate readOne(@PathVariable String configTemplateId) {
        return (ConfigTemplate) processReadOneQuery(configTemplateId);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Collection<? extends Resource> readAll(@RequestParam(required=false, defaultValue="") String name) {
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

    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody ConfigTemplate configTemplate) throws ParameterValidateException {
        return processCreateQuery(configTemplate);
    }

    @RequestMapping(value = "/{configTemplateId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<?> update(@PathVariable String configTemplateId,
                                    @RequestBody ConfigTemplate configTemplate)
                                    throws ParameterValidateException {
        return processUpdateQuery(configTemplateId, configTemplate);
    }

    @RequestMapping(value = "/{configTemplateId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable String configTemplateId) {
        return processDeleteQuery(configTemplateId);
    }
}
