package ru.majordomo.hms.rc.staff.importing;

import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;

@Service
@Profile("import")
public class ConfigTemplateDBSeedService {
    private final static Logger logger = LoggerFactory.getLogger(ConfigTemplateDBSeedService.class);

    private ConfigTemplateRepository configTemplateRepository;

    @Autowired
    public ConfigTemplateDBSeedService(ConfigTemplateRepository configTemplateRepository) {
        this.configTemplateRepository = configTemplateRepository;
    }

    public boolean seedDB() {
        configTemplateRepository.deleteAll();

        this.seed();

        return true;
    }

    private void seed() {
        Map<String, String> configTemplateNamesWithFileLink = ImmutableMap.<String, String>builder()
                .put("@NginxServer", "https://gitlab.intr/api/v3/projects/174/repository/files/?file_path=etc/nginx/sites-available/@NginxServer&ref=master")
                .put("@ApacheVHost", "https://gitlab.intr/api/v3/projects/174/repository/files/?file_path=etc/apache2/sites-available/@ApacheVHost&ref=master")
                .put("{config_base_path}/my.cnf", "https://gitlab.intr/api/v3/projects/174/repository/files/?file_path=etc/mysql/my.cnf&ref=master")
                .put("@HTTPErrorPage", "https://gitlab.intr/api/v3/projects/174/repository/files/?file_path=usr/share/nginx/html/@HTTPErrorPage&ref=master")
                .put("{config_base_path}/nginx.conf", "https://gitlab.intr/api/v3/projects/174/repository/files/?file_path=etc/nginx/nginx.conf&ref=master")
                .put("/etc/apparmor.d/usr.sbin.nginx", "https://gitlab.intr/api/v3/projects/174/repository/files/?file_path=etc/apparmor.d/usr.sbin.nginx&ref=master")
                .put("{config_base_path}/apache2.conf", "https://gitlab.intr/api/v3/projects/174/repository/files/?file_path=etc/apache2/apache2.conf&ref=master")
                .put("{config_base_path}/modules_conf.conf", "https://gitlab.intr/api/v3/projects/174/repository/files/?file_path=etc/apache2/modules_conf.conf&ref=master")
                .put("{config_base_path}/modules_load.conf", "https://gitlab.intr/api/v3/projects/174/repository/files/?file_path=etc/apache2/modules_load.conf&ref=master")
                .put("/opt/{interpreter.name}{interpreter.version_major}{interpreter.version_minor}/etc/{name}/php.ini", "https://gitlab.intr/api/v3/projects/174/repository/files/?file_path=opt/php/etc/apache2/php.ini&ref=master")
                .put("/etc/init/{name}.conf", "https://gitlab.intr/api/v3/projects/174/repository/files/?file_path=etc/init/apache2.conf&ref=master")
                .put("/etc/{name}/sites-available/000-default-vhost.conf", "https://gitlab.intr/api/v3/projects/174/repository/files/?file_path=etc/apache2/sites-available/000-default-vhost.conf&ref=master")
                .build();

        configTemplateNamesWithFileLink.forEach((name, fileLink) -> {
            ConfigTemplate configTemplate = new ConfigTemplate();
            configTemplate.setName(name);
            configTemplate.setFileLink(fileLink);

            configTemplateRepository.save(configTemplate);

            logger.debug(configTemplate.toString());
        });
    }
}
