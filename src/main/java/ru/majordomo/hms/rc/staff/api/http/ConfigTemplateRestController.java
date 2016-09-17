package ru.majordomo.hms.rc.staff.api.http;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;

@RestController
@CrossOrigin("*")
@RequestMapping("/rc/config-template")
public class ConfigTemplateRestController {
    @Autowired
    ConfigTemplateRepository configTemplateRepository;

    @RequestMapping(value = "/{configTemplateId}", method = RequestMethod.GET)
    public ConfigTemplate readOne(@PathVariable String configTemplateId) {
        return configTemplateRepository.findOne(configTemplateId);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Collection<ConfigTemplate> readAll() {
        return configTemplateRepository.findAll();
    }
}
