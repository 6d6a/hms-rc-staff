package ru.majordomo.hms.rc.staff.test.api.http;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;
import ru.majordomo.hms.rc.staff.test.config.EmbeddedServltetContainerConfig;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.ServiceTemplateServicesConfig;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoriesConfig.class, EmbeddedServltetContainerConfig.class, ServiceTemplateServicesConfig.class},
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ServiceTemplateRestControllerTest {
    @Autowired
    private ServiceTemplateRepository repository;
    @Autowired
    private ConfigTemplateRepository configTemplateRepository;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private WebApplicationContext ctx;
    @Value("${spring.application.name}")
    private String applicationName;
    private String resourceName = "service-template";
    private MockMvc mockMvc;
    private List<ServiceTemplate> serviceTemplates = new ArrayList<>();

    private void generateBatchOfServiceTemplates() {
        String namePattern = "Шаблон для сервиса ";
        Boolean switchedOn = Boolean.TRUE;
        for (int i = 1; i < 6; i++) {
            String name = namePattern + i;
            ConfigTemplate configTemplate = new ConfigTemplate();
            configTemplateRepository.save(configTemplate);
            ServiceTemplate serviceTemplate = new ServiceTemplate();
            serviceTemplate.setName(namePattern);
            serviceTemplate.setSwitchedOn(switchedOn);
            serviceTemplate.addConfigTemplate(configTemplate);
            repository.save(serviceTemplate);
            serviceTemplates.add(serviceTemplate);
        }
    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        repository.deleteAll();
        configTemplateRepository.deleteAll();
        generateBatchOfServiceTemplates();
    }

    @Test
    public void readOne() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName +
                                                "/" + resourceName +
                                                "/" + serviceTemplates.get(0).getId())
                                                .accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(status().isOk())
                                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAll() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName + "/"
                                                + resourceName + "/").accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readOneAndCheckObjectFields() {
        ServiceTemplate serviceTemplate = serviceTemplates.get(0);
        String testedServiceTemplateId = serviceTemplate.getId();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName
                                                + "/" + resourceName + "/" + testedServiceTemplateId)
                                                .accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(jsonPath("name").value(serviceTemplate.getName()))
                    .andExpect(jsonPath("id").value(serviceTemplate.getId()))
                    .andExpect(jsonPath("switchedOn").value(serviceTemplate.getSwitchedOn()))
                    .andExpect(jsonPath("configTemplates.[0]").value(serviceTemplate.getConfigTemplateIds().get(0)));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void create() {
        ServiceTemplate serviceTemplate = serviceTemplates.get(0);
        serviceTemplate.setId(null);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/" + applicationName +
                                                "/" + resourceName + "/")
                                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                .content(serviceTemplate.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isCreated());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void update() {
        ServiceTemplate serviceTemplate = serviceTemplates.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + applicationName
                                                + "/" + resourceName + "/" + serviceTemplate.getId())
                                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                .content(serviceTemplate.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isOk());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void updateNotExistingResource() {
        ServiceTemplate serviceTemplate = serviceTemplates.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + applicationName
                                                + "/" + resourceName + "/" + ObjectId.get().toString())
                                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                .content(serviceTemplate.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isNotFound());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void delete() {
        ServiceTemplate serviceTemplate = serviceTemplates.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + applicationName
                                                + "/" + resourceName + "/" + serviceTemplate.getId())
                                                .accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(status().isOk());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void deleteNotExisting() {
        String unknownServiceTemplateId = ObjectId.get().toString();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + applicationName
                                                + "/" + resourceName + "/" + unknownServiceTemplateId)
                                                .accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(status().isNotFound());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
