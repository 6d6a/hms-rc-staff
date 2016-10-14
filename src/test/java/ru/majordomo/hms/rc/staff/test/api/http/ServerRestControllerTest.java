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
import ru.majordomo.hms.rc.staff.repositories.ServerRoleRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.repositories.StorageRepository;
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
    private ServerRepository serverRepository;
    @Autowired
    private ServerRoleRepository serverRoleRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private StorageRepository storageRepository;

    @Value("${spring.application.name}")
    private String applicationName;
    private String resourceName = "server";
    private List<Server> testServers = new ArrayList<>();
    private MockMvc mockMvc;

    private void generateBatchOfServers() {
        for (int i = 1; i < 6; i++) {

            ServerRole serverRole = new ServerRole();
            serverRole.setId(ObjectId.get().toString());
            serverRoleRepository.save(serverRole);

            List<Service> services = new ArrayList<>();
            Service service = new Service();
            service.setId(ObjectId.get().toString());
            services.add(service);
            serviceRepository.save(services);

            List<Storage> storages = new ArrayList<>();
            Storage storage = new Storage();
            storage.setId(ObjectId.get().toString());
            storages.add(storage);
            storageRepository.save(storages);

            String name = "Сервер " + i;
            Boolean switchedOn = Boolean.TRUE;

            Server server = new Server();
            server.setName(name);
            server.setSwitchedOn(switchedOn);
            server.setServerRole(serverRole);
            server.setServices(services);
            server.setStorages(storages);

            serverRepository.save(server);
            testServers.add(server);
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
            "/" + resourceName + "/" + testServers.get(0).getId()).accept(MediaType.APPLICATION_JSON_UTF8);
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
        Server testingServer = testServers.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName +
            "/" + resourceName + "/" + testingServer.getId()).accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(jsonPath("id").value(testingServer.getId()))
                    .andExpect(jsonPath("name").value(testingServer.getName()))
                    .andExpect(jsonPath("switchedOn").value(testingServer.getSwitchedOn()))
                    .andExpect(jsonPath("serverRole").value(testingServer.getServerRoleId()))
                    .andExpect(jsonPath("services").isArray())
                    .andExpect(jsonPath("services.[0]").value(testingServer.getServices().get(0).getId()))
                    .andExpect(jsonPath("storages").isArray())
                    .andExpect(jsonPath("storages.[0]").value(testingServer.getStorages().get(0).getId()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void create() {
        Server server = testServers.get(0);
        server.setId(null);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/" + applicationName
                + "/" + resourceName)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(server.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isCreated());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void update() {
        Server server = testServers.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + applicationName
                + "/" + resourceName + "/" + server.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(server.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isOk());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void updateNotExistingResource() {
        Server server = testServers.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + applicationName
                + "/" + resourceName + "/" + ObjectId.get().toString())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(server.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isNotFound());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void delete() {
        Server server = testServers.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + applicationName
                + "/" + resourceName + "/" + server.getId())
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
