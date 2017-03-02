package ru.majordomo.hms.rc.staff.test.config;

import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ru.majordomo.hms.rc.staff.api.http.*;

@Configuration
@EnableWebMvc
public class ConfigOfRestControllers {
    @Bean
    public EmbeddedServletContainerFactory embeddedServletContainerFactory() {
        return new TomcatEmbeddedServletContainerFactory(0);
    }
    @Bean
    public ConfigTemplateRestController configTemplateRestController() {
        return new ConfigTemplateRestController();
    }
    @Bean
    public NetworkRestController networkRestController() {
        return new NetworkRestController();
    }
    @Bean
    public ServerRoleRestController serverRoleRestController() {
        return new ServerRoleRestController();
    }
    @Bean
    public ServerRestController serverRestController() {
        return new ServerRestController();
    }
    @Bean
    public ServiceRestController serviceRestController() {
        return new ServiceRestController();
    }
    @Bean
    public ServiceSocketRestController serviceSocketRestController() {
        return new ServiceSocketRestController();
    }
    @Bean
    public ServiceTemplateRestController serviceTemplateRestController() {
        return new ServiceTemplateRestController();
    }
    @Bean
    public ServiceTypeRestController serviceTypeRestController() {
        return new ServiceTypeRestController();
    }
    @Bean
    public StorageRestController storageRestController() {
        return new StorageRestController();
    }
}
