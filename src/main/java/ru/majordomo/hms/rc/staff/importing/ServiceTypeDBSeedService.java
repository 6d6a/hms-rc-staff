package ru.majordomo.hms.rc.staff.importing;

import com.google.common.collect.ImmutableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import ru.majordomo.hms.rc.staff.repositories.ServiceTypeRepository;
import ru.majordomo.hms.rc.staff.resources.ServiceType;

@Service
public class ServiceTypeDBSeedService {
    private final static Logger logger = LoggerFactory.getLogger(ServiceTypeDBSeedService.class);

    private ServiceTypeRepository serviceTypeRepository;

    @Autowired
    public ServiceTypeDBSeedService(ServiceTypeRepository serviceTypeRepository) {
        this.serviceTypeRepository = serviceTypeRepository;
    }

    public boolean seedDB() {
        serviceTypeRepository.deleteAll();

        this.seed();

        return true;
    }

    private void seed() {
        Set<String> websiteServiceTypeNames = ImmutableSet.<String>builder()
                .add("DATABASE_MYSQL")
                .add("STAFF_NGINX")
                .add("WEBSITE_APACHE2_PHP4_DEFAULT")
                .add("WEBSITE_APACHE2_PHP4_UNSAFE")
                .add("WEBSITE_APACHE2_PHP4_HARDENED_NOCHMOD")
                .add("WEBSITE_APACHE2_PHP4_HARDENED")
                .add("WEBSITE_APACHE2_PHP52_DEFAULT")
                .add("WEBSITE_APACHE2_PHP52_UNSAFE")
                .add("WEBSITE_APACHE2_PHP52_HARDENED_NOCHMOD")
                .add("WEBSITE_APACHE2_PHP52_HARDENED")
                .add("WEBSITE_APACHE2_PHP53_DEFAULT")
                .add("WEBSITE_APACHE2_PHP53_UNSAFE")
                .add("WEBSITE_APACHE2_PHP53_HARDENED_NOCHMOD")
                .add("WEBSITE_APACHE2_PHP53_HARDENED")
                .add("WEBSITE_APACHE2_PHP54_DEFAULT")
                .add("WEBSITE_APACHE2_PHP54_UNSAFE")
                .add("WEBSITE_APACHE2_PHP54_HARDENED_NOCHMOD")
                .add("WEBSITE_APACHE2_PHP54_HARDENED")
                .add("WEBSITE_APACHE2_PHP55_DEFAULT")
                .add("WEBSITE_APACHE2_PHP55_UNSAFE")
                .add("WEBSITE_APACHE2_PHP55_HARDENED_NOCHMOD")
                .add("WEBSITE_APACHE2_PHP55_HARDENED")
                .add("WEBSITE_APACHE2_PHP56_DEFAULT")
                .add("WEBSITE_APACHE2_PHP56_UNSAFE")
                .add("WEBSITE_APACHE2_PHP56_HARDENED_NOCHMOD")
                .add("WEBSITE_APACHE2_PHP56_HARDENED")
                .add("WEBSITE_APACHE2_PHP70_DEFAULT")
                .add("WEBSITE_APACHE2_PHP70_UNSAFE")
                .add("WEBSITE_APACHE2_PHP70_HARDENED_NOCHMOD")
                .add("WEBSITE_APACHE2_PHP70_HARDENED")
                .add("WEBSITE_APACHE2_PERL518")
                .build();

        websiteServiceTypeNames.forEach(s -> {
            ServiceType serviceType = new ServiceType();
            serviceType.setName(s);

            serviceTypeRepository.save(serviceType);

            logger.debug(serviceType.toString());
        });
    }
}
