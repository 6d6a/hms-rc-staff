package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfTemplate;
import ru.majordomo.hms.rc.staff.resources.template.ResourceType;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;
import ru.majordomo.hms.rc.staff.resources.template.*;

import java.util.*;

@RestController
public class TemplateRestController extends RestControllerTemplate<Template> {
    private MongoOperations mongoOperations;

    @Autowired
    public void setMongoOperations(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Autowired
    public void setGovernor(GovernorOfTemplate governor) {
        this.governor = governor;
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_VIEW')")
    @RequestMapping(value = "/template/{templateId}", method = RequestMethod.GET)
    public Template readOne(@PathVariable String templateId) {
        return processReadOneQuery(templateId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_VIEW')")
    @RequestMapping(value = "/template", method = RequestMethod.GET)
    public Collection<Template> readAll(@RequestParam(required=false, defaultValue="") String name) {
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

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_VIEW')")
    @RequestMapping(value = "/template", method = RequestMethod.GET, headers = "X-HMS-Pageable=true")
    public Page<Template> readAll(
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

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_VIEW')")
    @RequestMapping(value = "/template", method = RequestMethod.GET, headers = "X-HMS-Projection=OnlyIdAndName")
    public Collection<Template> readAll() {
        return processReadAllQueryOnlyIdAndName();
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_CREATE')")
    @RequestMapping(value = "/template", method = RequestMethod.POST)
    public ResponseEntity<Template> create(@RequestBody Template template) throws ParameterValidateException {
        return processCreateQuery(template);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_EDIT')")
    @RequestMapping(value = "/template/{templateId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<Template> update(
            @PathVariable String templateId,
            @RequestBody Template template
    ) throws ParameterValidateException {
        return processUpdateQuery(templateId, template);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_TEMPLATE_DELETE')")
    @RequestMapping(value = "/template/{templateId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable String templateId) {
        return processDeleteQuery(templateId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR') or (hasRole('USER') and #accountId == principal.accountId)")
    @GetMapping("/{accountId}/template")
    public Collection<Template> readAllByAccountId(
            @PathVariable String accountId,
            @RequestParam Map<String,String> requestParams
    ) {
        requestParams.put("availableToAccounts", "true");

        return governor.buildAll(requestParams);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR') or (hasRole('USER') and #accountId == principal.accountId)")
    @GetMapping("/{accountId}/template/{templateId}")
    public Template readOneByAccountId(
            @PathVariable String accountId,
            @PathVariable String templateId
    ) {
        Map<String, String> requestParams = new HashMap<>();

        requestParams.put("resourceId", templateId);
        requestParams.put("availableToAccounts", "true");

        return processReadOneWithParamsQuery(requestParams);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/template/migrate")
    public Collection<Template> migrate() {
        List<ServiceTemplate> serviceTemplates = mongoOperations.findAll(ServiceTemplate.class);

        List<Template> templates = new ArrayList<>();

        serviceTemplates.forEach(serviceTemplate -> {
            String name = serviceTemplate.getName().endsWith("@docker") ? serviceTemplate.getName().replace("@docker", "") : serviceTemplate.getName();

            switch (serviceTemplate.getServiceTypeName()) {
                case "STAFF_NGINX":
                    HttpServer httpServer = new HttpServer();
                    httpServer.switchedOn = true;
                    httpServer.setAvailableToAccounts(false);
//                    httpServer.addProvidedResourceConfig(ResourceType.WEBSITE, new HashMap<>());
                    httpServer.setSupervisionType(Template.SupervisionType.DOCKER);
                    httpServer.setName(name);
                    httpServer.setConfigTemplateIds(serviceTemplate.getConfigTemplateIds());
                    httpServer.addMigratedServiceTemplateId(serviceTemplate.getId());

                    templates.add(httpServer);

                    break;
                case "DATABASE_MYSQL":
                    DatabaseServer databaseServer = new DatabaseServer();
                    databaseServer.switchedOn = true;
                    databaseServer.setAvailableToAccounts(false);
                    databaseServer.setType(DatabaseServer.Type.MYSQL);
//                    databaseServer.addProvidedResourceConfig(ResourceType.DATABASE, new HashMap<>());
                    databaseServer.setSupervisionType(Template.SupervisionType.SYSVINIT);
                    databaseServer.setName(name);
                    databaseServer.setConfigTemplateIds(serviceTemplate.getConfigTemplateIds());
                    databaseServer.addMigratedServiceTemplateId(serviceTemplate.getId());

                    templates.add(databaseServer);

                    break;
                case "ACCESS_SSH_GUEST_ROOM":
                case "ACCESS_SSH_SUP_ROOM":
                    SshD sshD = new SshD();
                    sshD.switchedOn = true;
                    sshD.setAvailableToAccounts(false);
//                    sshD.addProvidedResourceConfig(ResourceType.ACCESS, new HashMap<>());
                    sshD.setSupervisionType(Template.SupervisionType.DOCKER);
                    sshD.setName(name);
                    sshD.setConfigTemplateIds(serviceTemplate.getConfigTemplateIds());
                    sshD.addMigratedServiceTemplateId(serviceTemplate.getId());

                    templates.add(sshD);

                    break;

                case "ACCESS_FTPSERVER":
                    FtpD ftpD = new FtpD();
                    ftpD.switchedOn = true;
                    ftpD.setAvailableToAccounts(false);
//                    ftpD.addProvidedResourceConfig(ResourceType.ACCESS, new HashMap<>());
                    ftpD.setSupervisionType(Template.SupervisionType.DOCKER);
                    ftpD.setName(name);
                    ftpD.setConfigTemplateIds(serviceTemplate.getConfigTemplateIds());
                    ftpD.addMigratedServiceTemplateId(serviceTemplate.getId());

                    templates.add(ftpD);

                    break;
                case "STAFF_POSTFIX":
                    Postfix postfix = new Postfix();
                    postfix.switchedOn = true;
                    postfix.setAvailableToAccounts(false);
//                    postfix.addProvidedResourceConfig(ResourceType.STAFF, new HashMap<>());
                    postfix.setSupervisionType(Template.SupervisionType.DOCKER);
                    postfix.setName(name);
                    postfix.setConfigTemplateIds(serviceTemplate.getConfigTemplateIds());
                    postfix.addMigratedServiceTemplateId(serviceTemplate.getId());

                    templates.add(postfix);

                    break;
                case "STAFF_CRON":
                    CronD cronD = new CronD();
                    cronD.switchedOn = true;
                    cronD.setAvailableToAccounts(false);
//                    cronD.addProvidedResourceConfig(ResourceType.STAFF, new HashMap<>());
                    cronD.setSupervisionType(Template.SupervisionType.DOCKER);
                    cronD.setName(name);
                    cronD.setConfigTemplateIds(serviceTemplate.getConfigTemplateIds());
                    cronD.addMigratedServiceTemplateId(serviceTemplate.getId());

                    templates.add(cronD);

                    break;
                default:
                    String[] parts = serviceTemplate.getServiceTypeName().split("_", 4);

                    if (parts[2].startsWith("PHP")) {
                        String[] nameParts = name.split("-");
                        name = nameParts[0] + "-" + nameParts[1];
                        ApplicationServer applicationServer = new ApplicationServer();
                        applicationServer.switchedOn = true;
                        applicationServer.setAvailableToAccounts(false);
                        applicationServer.setLanguage(ApplicationServer.Language.PHP);
                        applicationServer.addInstanceSpec(ApplicationServer.Spec.SECURITY_LEVEL, Template.InstanceSpecType.STRING);
//                        applicationServer.addProvidedResourceConfig(ResourceType.WEBSITE, new HashMap<>());
                        applicationServer.setSupervisionType(serviceTemplate.getName().endsWith("@docker") ? Template.SupervisionType.DOCKER : Template.SupervisionType.UPSTART);
                        applicationServer.setName(name);
                        applicationServer.setConfigTemplateIds(serviceTemplate.getConfigTemplateIds());
                        applicationServer.addMigratedServiceTemplateId(serviceTemplate.getId());

                        switch (parts[2]) {
                            case "PHP4":
                                applicationServer.setVersion("4");

                                break;
                            case "PHP52":
                                applicationServer.setVersion("5.2");

                                break;
                            case "PHP53":
                                applicationServer.setVersion("5.3");

                                break;
                            case "PHP54":
                                applicationServer.setVersion("5.4");

                                break;
                            case "PHP55":
                                applicationServer.setVersion("5.5");

                                break;
                            case "PHP56":
                                applicationServer.setVersion("5.6");

                                break;
                            case "PHP70":
                                applicationServer.setVersion("7.0");

                                break;
                            case "PHP71":
                                applicationServer.setVersion("7.1");

                                break;
                            case "PHP72":
                                applicationServer.setVersion("7.2");

                                break;
                            case "PHP73":
                                applicationServer.setVersion("7.3");

                                break;
                        }

                        ApplicationServer alreadyFoundSameVersionApplicationServer = templates.stream()
                                .filter(template -> template instanceof ApplicationServer
                                        && ((ApplicationServer) template).getVersion().equals(applicationServer.getVersion())
                                        && template.getSupervisionType().equals(applicationServer.getSupervisionType()))
                                .map(template -> (ApplicationServer) template)
                                .findFirst()
                                .orElse(null);
                        if (alreadyFoundSameVersionApplicationServer != null) {
                            alreadyFoundSameVersionApplicationServer.addMigratedServiceTemplateId(serviceTemplate.getId());
                        } else {
                            templates.add(applicationServer);
                        }
                    } else if (parts[2].startsWith("PERL")){
                        ApplicationServer applicationServer = new ApplicationServer();
                        applicationServer.switchedOn = true;
                        applicationServer.setAvailableToAccounts(false);
                        applicationServer.setLanguage(ApplicationServer.Language.PERL);
//                        applicationServer.addProvidedResourceConfig(ResourceType.WEBSITE, new HashMap<>());
                        applicationServer.setSupervisionType(serviceTemplate.getName().endsWith("@docker") ? Template.SupervisionType.DOCKER : Template.SupervisionType.UPSTART);
                        applicationServer.setName(name);
                        applicationServer.setConfigTemplateIds(serviceTemplate.getConfigTemplateIds());
                        applicationServer.addMigratedServiceTemplateId(serviceTemplate.getId());
                        applicationServer.setVersion("5.18");

                        templates.add(applicationServer);
                    }
            }
        });

        mongoOperations.insertAll(templates);

        return templates;
    }
}
