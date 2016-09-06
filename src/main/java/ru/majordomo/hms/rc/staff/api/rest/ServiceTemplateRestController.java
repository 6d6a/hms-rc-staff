package ru.majordomo.hms.rc.staff.api.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@RestController
@RequestMapping("/rc/staff/service-template")
public class ServiceTemplateRestController {
    @RequestMapping(method = RequestMethod.GET)
    public List<ServiceTemplate> get() {

    }
}
