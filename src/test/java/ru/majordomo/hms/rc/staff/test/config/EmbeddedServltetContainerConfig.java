package ru.majordomo.hms.rc.staff.test.config;

import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddedServltetContainerConfig {
    @Bean
    public EmbeddedServletContainerFactory embeddedServletContainerFactory() {
        return new TomcatEmbeddedServletContainerFactory(0);
    }
}
