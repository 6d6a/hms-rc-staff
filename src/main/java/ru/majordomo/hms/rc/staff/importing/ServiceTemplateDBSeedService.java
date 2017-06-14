package ru.majordomo.hms.rc.staff.importing;

import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@Service
public class ServiceTemplateDBSeedService {
    private final static Logger logger = LoggerFactory.getLogger(ServiceTemplateDBSeedService.class);

    private final ServiceTemplateRepository serviceTemplateRepository;
    private final ConfigTemplateRepository configTemplateRepository;

    @Autowired
    public ServiceTemplateDBSeedService(
            ServiceTemplateRepository serviceTemplateRepository,
            ConfigTemplateRepository configTemplateRepository
    ) {
        this.serviceTemplateRepository = serviceTemplateRepository;
        this.configTemplateRepository = configTemplateRepository;
    }

    public boolean seedDB() {
        serviceTemplateRepository.deleteAll();

        this.seed();

        return true;
    }

    private void seed() {
        Map<String, List<String>> serviceTemplateNamesWithConfigTemplates = ImmutableMap.<String, List<String>>builder()
                .put("nginx:STAFF_NGINX", Arrays.asList(
                        "@NginxServer",
                        "@HTTPErrorPage",
                        "{config_base_path}/nginx.conf",
                        "/etc/apparmor.d/usr.sbin.nginx"
                        )
                )
                .put("mysql:DATABASE_MYSQL", Collections.singletonList("{config_base_path}/my.cnf"))
                .put("apache2-php4-default:WEBSITE_APACHE2_PHP4_DEFAULT", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                        )
                )
                .put("apache2-php4-unsafe:WEBSITE_APACHE2_PHP4_UNSAFE", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php4-hardened_nochmod:WEBSITE_APACHE2_PHP4_HARDENED_NOCHMOD", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php4-hardened:WEBSITE_APACHE2_PHP4_HARDENED", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php52-default:WEBSITE_APACHE2_PHP52_DEFAULT", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php52-unsafe:WEBSITE_APACHE2_PHP52_UNSAFE", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php52-hardened_nochmod:WEBSITE_APACHE2_PHP52_HARDENED_NOCHMOD", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php52-hardened:WEBSITE_APACHE2_PHP52_HARDENED", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php53-default:WEBSITE_APACHE2_PHP53_DEFAULT", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php53-unsafe:WEBSITE_APACHE2_PHP53_UNSAFE", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php53-hardened_nochmod:WEBSITE_APACHE2_PHP53_HARDENED_NOCHMOD", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php53-hardened:WEBSITE_APACHE2_PHP53_HARDENED", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php54-default:WEBSITE_APACHE2_PHP54_DEFAULT", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php54-unsafe:WEBSITE_APACHE2_PHP54_UNSAFE", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php54-hardened_nochmod:WEBSITE_APACHE2_PHP54_HARDENED_NOCHMOD", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php54-hardened:WEBSITE_APACHE2_PHP54_HARDENED", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php55-default:WEBSITE_APACHE2_PHP55_DEFAULT", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php55-unsafe:WEBSITE_APACHE2_PHP55_UNSAFE", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php55-hardened_nochmod:WEBSITE_APACHE2_PHP55_HARDENED_NOCHMOD", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php55-hardened:WEBSITE_APACHE2_PHP55_HARDENED", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php56-default:WEBSITE_APACHE2_PHP56_DEFAULT", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php56-unsafe:WEBSITE_APACHE2_PHP56_UNSAFE", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php56-hardened_nochmod:WEBSITE_APACHE2_PHP56_HARDENED_NOCHMOD", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php56-hardened:WEBSITE_APACHE2_PHP56_HARDENED", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php70-default:WEBSITE_APACHE2_PHP70_DEFAULT", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php70-unsafe:WEBSITE_APACHE2_PHP70_UNSAFE", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php70-hardened_nochmod:WEBSITE_APACHE2_PHP70_HARDENED_NOCHMOD", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-php70-hardened:WEBSITE_APACHE2_PHP70_HARDENED", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .put("apache2-perl518:WEBSITE_APACHE2_PERL518", Arrays.asList(
                        "@ApacheVHost",
                        "{config_base_path}/apache2.conf",
                        "{config_base_path}/modules_conf.conf",
                        "{config_base_path}/modules_load.conf",
                        "/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini",
                        "/etc/init/{name}.conf",
                        "/etc/{name}/sites-available/000-default-vhost.conf"
                ))
                .build();

        List<ConfigTemplate> configTemplates = configTemplateRepository.findAll();

        serviceTemplateNamesWithConfigTemplates.forEach((name, configTemplateNames) -> {
            ServiceTemplate serviceTemplate = new ServiceTemplate();
            String[] names = name.split(":");
            serviceTemplate.setName(names[0]);
            serviceTemplate.setServiceTypeName(names[1]);
            serviceTemplate.setSwitchedOn(true);

            List<String> configTemplateIds = new ArrayList<>();

            for (String configTemplateName : configTemplateNames) {
                configTemplateIds.addAll(
                        configTemplates
                                .stream()
                                .filter(configTemplate -> configTemplate.getName().equals(configTemplateName))
                                .map(Resource::getId)
                                .collect(Collectors.toList())
                );
            }

            serviceTemplate.setConfigTemplateIds(configTemplateIds);

            serviceTemplateRepository.save(serviceTemplate);

            logger.debug(serviceTemplate.toString());
        });
    }
}
