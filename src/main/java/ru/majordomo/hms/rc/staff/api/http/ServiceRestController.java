package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfService;
import ru.majordomo.hms.rc.staff.repositories.template.TemplateRepository;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.template.ApplicationServer;
import ru.majordomo.hms.rc.staff.resources.template.Template;

@RestController
public class ServiceRestController extends RestControllerTemplate<Service> {
    private TemplateRepository templateRepository;
    private MongoOperations mongoOperations;
    private GovernorOfService governorOfService;

    @Autowired
    public void setTemplateRepository(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Autowired
    public void setMongoOperations(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Autowired
    public void setGovernor(GovernorOfService governor) {
        this.governor = governor;
        governorOfService = governor;
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_VIEW')")
    @RequestMapping(value = "/service/{serviceId}", method = RequestMethod.GET)
    public Service readOne(@PathVariable String serviceId) {
        return processReadOneQuery(serviceId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_VIEW')")
    @RequestMapping(value = {"/service", "/service/"}, method = RequestMethod.GET)
    public Collection<Service> readAll(
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

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_VIEW')")
    @RequestMapping(value = {"/service", "/service/"}, method = RequestMethod.GET, headers = "X-HMS-Pageable=true")
    public Page<Service> readAll(
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

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_VIEW')")
    @RequestMapping(value = {"/service", "/service/"}, method = RequestMethod.GET, headers = "X-HMS-Projection=OnlyIdAndName")
    public Collection<Service> readAll() {
        return processReadAllQueryOnlyIdAndName();
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

    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR') or (hasRole('USER') and #accountId == principal.accountId)")
    @GetMapping("/{accountId}/server/{serverId}/services")
    public Collection<Service> readAllByAccountId(
            @PathVariable String serverId,
            @PathVariable String accountId,
            @RequestParam Map<String,String> requestParams
    ) {
        requestParams.put("serverId", serverId);
        requestParams.put("accountId", accountId);

        return processReadAllWithParamsQuery(requestParams);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR') or (hasRole('USER') and #accountId == principal.accountId)")
    @GetMapping({"/{accountId}/service", "/{accountId}/staff/service"})
    public Collection<Service> readOnlyByAccountId(
            @PathVariable String accountId,
            @RequestParam Map<String,String> requestParams
    ) {
        requestParams.put("accountId", accountId);

        return processReadAllWithParamsQuery(requestParams);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_VIEW')")
    @GetMapping({"/{accountId}/service/{serviceId}", "/{accountId}/staff/service/{serviceId}"})
    public Service readOneByAccountId(
            @PathVariable String accountId,
            @PathVariable String serviceId
    ) {
        Map<String, String> requestParams = new HashMap<>();

        requestParams.put("resourceId", serviceId);
        requestParams.put("accountId", accountId);

        return processReadOneWithParamsQuery(requestParams);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/service/migrate")
    public Collection<Service> migrate() {
        List<Service> services = governor.buildAll();

        services.forEach(service -> {
            service.setSocketIds(service.getServiceSocketIds());

            Template template = templateRepository.findFirstByMigratedServiceTemplateIds(service.getServiceTemplateId());

            if (template != null) {
                service.setTemplateId(template.getId());
            }

            if (template instanceof ApplicationServer && ((ApplicationServer) template).getLanguage() == ApplicationServer.Language.PHP) {
                String serviceTemplateName = service.getServiceTemplate().getName();
                serviceTemplateName = serviceTemplateName.replace("@docker", "");
                String[] nameParts = serviceTemplateName.split("-");
                String config = nameParts[2];
                service.addInstanceProp(ApplicationServer.Spec.SECURITY_LEVEL, config);
            }

            mongoOperations.save(service);
        });

        return services;
    }

    @Override
    protected ResponseEntity<Service> processUpdateQuery(String resourceId, Service resource) throws ParameterValidateException {
        if (governorOfService.exists(resourceId)) {
            resource.setId(resourceId);
            governorOfService.isValid(resource);
            governorOfService.save(resource, true);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
