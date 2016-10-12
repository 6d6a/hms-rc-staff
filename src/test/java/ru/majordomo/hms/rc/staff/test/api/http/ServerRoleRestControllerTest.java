package ru.majordomo.hms.rc.staff.test.api.http;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import ru.majordomo.hms.rc.staff.repositories.ServerRoleRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ServerRole;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;
import ru.majordomo.hms.rc.staff.test.config.EmbeddedServltetContainerConfig;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.ServerRoleServicesConfig;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoriesConfig.class, EmbeddedServltetContainerConfig.class, ServerRoleServicesConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ServerRoleRestControllerTest {
    @Autowired
    WebApplicationContext ctx;
    @Autowired
    private ServerRoleRepository repository;
    @Autowired
    private ServiceTemplateRepository templateRepository;

    @Value("${spring.application.name}")
    private String applicationName;
    private String resourceName = "server-role";
    private List<ServerRole> testServerRoles = new ArrayList<>();
    private MockMvc mockMvc;

    private static final Logger logger = LoggerFactory.getLogger(ServerRoleRestControllerTest.class);

    private void generateBatchOfServerRoles() {
        for (int i = 1; i < 6; i++) {
            //создать сервис темплейт и сохранить его
            ServiceTemplate template = new ServiceTemplate();
            templateRepository.save(template);
            // создать сервер роль без сохранения
            String name = "Серверная роль " + i;
            Boolean switchedOn = Boolean.TRUE;
            ServerRole serverRole = new ServerRole();
            serverRole.setName(name);
            serverRole.setSwitchedOn(switchedOn);
            serverRole.addServiceTemplate(template);
            repository.save(serverRole);
            testServerRoles.add(serverRole);
        }
    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        generateBatchOfServerRoles();
    }

    @Test
    public void readOne() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName +
            "/" + resourceName + "/" + testServerRoles.get(0).getId()).accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAll() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName +
                "/" + resourceName).accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$").isArray());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readOneAndCheckObjectFields() {
        ServerRole testingServerRole = testServerRoles.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName +
                "/" + resourceName + "/" + testingServerRole.getId()).accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(jsonPath("id").value(testingServerRole.getId()))
                    .andExpect(jsonPath("name").value(testingServerRole.getName()))
                    .andExpect(jsonPath("switchedOn").value(testingServerRole.getSwitchedOn()))
                    .andExpect(jsonPath("serviceTemplates").isArray())
                    .andExpect(jsonPath("serviceTemplates.[0]").value(testingServerRole.getServiceTemplates().get(0).getId()))
                    .andDo(print());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void create() {
        ServerRole serverRole = testServerRoles.get(0);
        serverRole.setId(null);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/" + applicationName
                + "/" + resourceName)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(serverRole.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isCreated());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void update() {
        ServerRole serverRole = testServerRoles.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + applicationName
                + "/" + resourceName + "/" + serverRole.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(serverRole.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isOk());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void updateNotExistingResource() {
        ServerRole serverRole = testServerRoles.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + applicationName
                + "/" + resourceName + "/" + ObjectId.get().toString())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(serverRole.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isNotFound());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void delete() {
        ServerRole serverRole = testServerRoles.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + applicationName
                + "/" + resourceName + "/" + serverRole.getId())
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
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + applicationName
                + "/" + resourceName + "/" + ObjectId.get().toString())
                .accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(status().isNotFound());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
