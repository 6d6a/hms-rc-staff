package ru.majordomo.hms.rc.staff.test.api.http;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import ru.majordomo.hms.rc.staff.repositories.ServerRepository;
import ru.majordomo.hms.rc.staff.resources.Server;
import ru.majordomo.hms.rc.staff.resources.ServerRole;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.Storage;
import ru.majordomo.hms.rc.staff.test.config.EmbeddedServltetContainerConfig;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.ServerServicesConfig;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoriesConfig.class, EmbeddedServltetContainerConfig.class, ServerServicesConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ServerRestControllerTest {
    @Autowired
    private WebApplicationContext ctx;
    @Autowired
    private ServerRepository repository;

    @Value("${spring.application.name}")
    private String applicationName;
    private String resourceName = "server";
    private List<Server> testServerList = new ArrayList<>();
    private MockMvc mockMvc;

    private void generateBatchOfServers() {
        for (int i = 1; i < 6; i++) {
            // создать сервер роль
            List<ServerRole> serverRoleList = new ArrayList<>();
            ServerRole serverRole = new ServerRole();
            serverRole.setId(ObjectId.get().toString());
            serverRoleList.add(serverRole);
            // создать сервис
            List<Service> serviceList = new ArrayList<>();
            Service service = new Service();
            service.setId(ObjectId.get().toString());
            serviceList.add(service);
            // создать сторейдж
            List<Storage> storageList = new ArrayList<>();
            Storage storage = new Storage();
            storage.setId(ObjectId.get().toString());
            storageList.add(storage);

            // создать сервер
            Server server = new Server();
            server.setServerRole(serverRole);
            server.setServiceList(serviceList);
            server.setStorageList(storageList);

            repository.save(server);
            testServerList.add(server);
        }
    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        generateBatchOfServers();
    }

    @Test
    public void readOne() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName +
            "/" + resourceName + "/" + testServerList.get(0).getId()).accept(MediaType.APPLICATION_JSON_UTF8);
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
                "/" + resourceName ).accept(MediaType.APPLICATION_JSON_UTF8);
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
        Server testingServer = testServerList.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName +
            "/" + resourceName + "/" + testingServer.getId()).accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(jsonPath("id").value(testingServer.getId()))
                    .andExpect(jsonPath("name").value(testingServer.getName()))
                    .andExpect(jsonPath("switchedOn").value(testingServer.getSwitchedOn()))
                    .andExpect(jsonPath("serverRole").value(testingServer.getServerRoleId()))
                    .andExpect(jsonPath("serviceList.[0]").value(testingServer.getServiceList().get(0)))
                    .andExpect(jsonPath("storageList.[0]").value(testingServer.getStorageList().get(0)));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
