package ru.majordomo.hms.rc.staff.importing;

import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceSocketRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@Component
public class ServiceDBSeedService {
    private final static Logger logger = LoggerFactory.getLogger(ServiceDBSeedService.class);

    private final ServiceRepository serviceRepository;
    private final ServiceTemplateRepository serviceTemplateRepository;
    private final ServiceSocketRepository serviceSocketRepository;

    @Autowired
    public ServiceDBSeedService(
            ServiceRepository serviceRepository,
            ServiceTemplateRepository serviceTemplateRepository,
            ServiceSocketRepository serviceSocketRepository
    ) {
        this.serviceRepository = serviceRepository;
        this.serviceTemplateRepository = serviceTemplateRepository;
        this.serviceSocketRepository = serviceSocketRepository;
    }

    public boolean seedDB() {
        serviceRepository.deleteAll();

        this.seed();

        return true;
    }

    private void seed() {
        Map<String, List<String>> serviceNamesWithServiceSockets = ImmutableMap.<String, List<String>>builder()
                .put("nginx:STAFF_NGINX", Arrays.asList(
                        "@NginxServer",
                        "@HTTPErrorPage",
                        "{config_base_path}/nginx.conf",
                        "/etc/apparmor.d/usr.sbin.nginx"
                        )
                )
                .put("mysql:DATABASE_MYSQL", Collections.singletonList("{config_base_path}/my.cnf"))
                .put("apache2-php4-default:WEBSITE_APACHE2_PHP4_DEFAULT", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php4-default:WEBSITE_APACHE2_PHP4_UNSAFE", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php4-default:WEBSITE_APACHE2_PHP4_HARDENED_NOCHMOD", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php4-default:WEBSITE_APACHE2_PHP4_HARDENED", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php5-default:WEBSITE_APACHE2_PHP5_DEFAULT", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php5-default:WEBSITE_APACHE2_PHP5_UNSAFE", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php5-default:WEBSITE_APACHE2_PHP5_HARDENED_NOCHMOD", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php5-default:WEBSITE_APACHE2_PHP5_HARDENED", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php53-default:WEBSITE_APACHE2_PHP53_DEFAULT", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php53-default:WEBSITE_APACHE2_PHP53_UNSAFE", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php53-default:WEBSITE_APACHE2_PHP53_HARDENED_NOCHMOD", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php53-default:WEBSITE_APACHE2_PHP53_HARDENED", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php54-default:WEBSITE_APACHE2_PHP54_DEFAULT", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php54-default:WEBSITE_APACHE2_PHP54_UNSAFE", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php54-default:WEBSITE_APACHE2_PHP54_HARDENED_NOCHMOD", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php54-default:WEBSITE_APACHE2_PHP54_HARDENED", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php55-default:WEBSITE_APACHE2_PHP55_DEFAULT", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php55-default:WEBSITE_APACHE2_PHP55_UNSAFE", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php55-default:WEBSITE_APACHE2_PHP55_HARDENED_NOCHMOD", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php55-default:WEBSITE_APACHE2_PHP55_HARDENED", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php56-default:WEBSITE_APACHE2_PHP56_DEFAULT", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php56-default:WEBSITE_APACHE2_PHP56_UNSAFE", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php56-default:WEBSITE_APACHE2_PHP56_HARDENED_NOCHMOD", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php56-default:WEBSITE_APACHE2_PHP56_HARDENED", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php70-default:WEBSITE_APACHE2_PHP70_DEFAULT", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php70-default:WEBSITE_APACHE2_PHP70_UNSAFE", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php70-default:WEBSITE_APACHE2_PHP70_HARDENED_NOCHMOD", Collections.singletonList("@ApacheVHost"))
                .put("apache2-php70-default:WEBSITE_APACHE2_PHP70_HARDENED", Collections.singletonList("@ApacheVHost"))
                .put("apache2-perl518:WEBSITE_APACHE2_PERL518", Collections.singletonList("@ApacheVHost"))
                .build();

        List<ServiceTemplate> serviceTemplates = serviceTemplateRepository.findAll();
        List<ServiceSocket> serviceSockets = serviceSocketRepository.findAll();

        serviceNamesWithServiceSockets.forEach((name, serviceSocketNames) -> {
            Service service = new Service();
            String[] names = name.split(":");
            service.setName(names[0]);
//            service.setServiceTemplateId();
//            service.setServiceTypeName(names[1]);

            List<String> configTemplateIds = new ArrayList<>();

            for (String configTemplateName : serviceSocketNames) {
                configTemplateIds.addAll(
                        serviceSockets
                                .stream()
                                .filter(configTemplate -> configTemplate.getName().equals(configTemplateName))
                                .map(Resource::getId)
                                .collect(Collectors.toList())
                );
            }

//            service.setConfigTemplateIds(configTemplateIds);

            serviceRepository.save(service);

            logger.debug(service.toString());
        });
    }
}
