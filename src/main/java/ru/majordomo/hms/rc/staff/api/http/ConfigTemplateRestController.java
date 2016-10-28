package ru.majordomo.hms.rc.staff.api.http;


import org.bouncycastle.cert.ocsp.Req;
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
import ru.majordomo.hms.rc.staff.managers.GovernorOfConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;

@RestController
@CrossOrigin("*")
@RequestMapping("/${spring.application.name}/config-template")
public class ConfigTemplateRestController {

    private GovernorOfConfigTemplate governor;
    private String applicationName;

    @Autowired
    public void setGovernor(GovernorOfConfigTemplate governor) {
        this.governor = governor;
    }

    @Value("${spring.application.name}")
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @RequestMapping(value = "/{configTemplateId}", method = RequestMethod.GET)
    public ConfigTemplate readOne(@PathVariable String configTemplateId) {
        return (ConfigTemplate) governor.build(configTemplateId);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Collection<ConfigTemplate> readAll() {
        return governor.build();
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<?> create(@RequestBody ConfigTemplate configTemplate) throws ParameterValidateException {
        governor.isValid(configTemplate);
        ConfigTemplate createdConfigTemplate = (ConfigTemplate) governor.save(configTemplate);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdConfigTemplate.getId()).toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{configTemplateId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<?> update(@PathVariable String configTemplateId,
                                    @RequestBody ConfigTemplate configTemplate)
                                    throws ParameterValidateException {
        governor.isValid(configTemplate);
        try {
            ConfigTemplate storedConfigTemplate = (ConfigTemplate) governor.build(configTemplateId);
            configTemplate.setId(storedConfigTemplate.getId());
            governor.save(configTemplate);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{configTemplateId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable String configTemplateId) {
        try {
            ConfigTemplate configTemplate = (ConfigTemplate) governor.build(configTemplateId);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        governor.delete(configTemplateId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
