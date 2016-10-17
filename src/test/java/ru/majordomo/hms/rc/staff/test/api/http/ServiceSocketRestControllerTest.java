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

import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.resources.Network;
import ru.majordomo.hms.rc.staff.test.config.EmbeddedServltetContainerConfig;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.ServiceSocketServicesConfig;
import ru.majordomo.hms.rc.staff.repositories.ServiceSocketRepository;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoriesConfig.class, ServiceSocketServicesConfig.class, EmbeddedServltetContainerConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ServiceSocketRestControllerTest {

    @Autowired
    private ServiceSocketRepository repository;
    @Autowired
    private NetworkRepository networkRepository;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private WebApplicationContext ctx;
    @Value("${spring.application.name}")
    private String applicationName;
    private String resourceName = "service-socket";

    private MockMvc mockMvc;
    private List<ServiceSocket> serviceSocketList = new ArrayList<>();

    private void generateBatchOfSockets() {

        String namePattern = "Сокет для сервиса";
        Boolean switchedOn = Boolean.TRUE;
        String addressPattern = "172.16.103.";
        Integer portPattern = 4000;

        Network network = new Network();
        network.setAddress(addressPattern + "0");
        network.setMask(24);
        networkRepository.save(network);

        for (int i = 2; i < 10; i++) {
            String name = namePattern + " " + i;
            String address = addressPattern + i;
            Integer port = portPattern + i;
            ServiceSocket serviceSocket = new ServiceSocket();
            serviceSocket.setName(name);
            serviceSocket.setSwitchedOn(switchedOn);
            serviceSocket.setAddress(address);
            serviceSocket.setPort(port);

            repository.save(serviceSocket);
            serviceSocketList.add(serviceSocket);
        }
    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        repository.deleteAll();
        networkRepository.deleteAll();
        generateBatchOfSockets();
    }

    @Test
    public void readOne() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName + "/" + resourceName + "/" + serviceSocketList.get(0).getId()).accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAll() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName + "/" + resourceName + "/").accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readOneAndCheckObjectFields() {

        try {
            ServiceSocket testingServiceSocket = serviceSocketList.get(0);
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName + "/" + resourceName + "/" + testingServiceSocket.getId()).accept(MediaType.APPLICATION_JSON_UTF8);
            mockMvc.perform(request).andExpect(jsonPath("port").value(testingServiceSocket.getPort()))
                    .andExpect(jsonPath("address").value(testingServiceSocket.getAddressAsString()))
                    .andExpect(jsonPath("switchedOn").value(testingServiceSocket.getSwitchedOn()))
                    .andExpect(jsonPath("name").value(testingServiceSocket.getName()))
                    .andExpect(jsonPath("id").value(testingServiceSocket.getId()));
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void create() {
        try {
            ServiceSocket testingServiceSocket = serviceSocketList.get(0);
            testingServiceSocket.setId(null);
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/" + applicationName + "/" + resourceName + "/")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(testingServiceSocket.toJson());
            mockMvc.perform(request).andExpect(status().isCreated());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void createWithUnknownNetwork() {
        try {
            ServiceSocket testingServiceSocket = serviceSocketList.get(0);
            testingServiceSocket.setId(null);
            testingServiceSocket.setAddress("10.10.10.10");
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/" + applicationName + "/" + resourceName + "/")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(testingServiceSocket.toJson());
            mockMvc.perform(request).andExpect(status().isBadRequest());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void update() {
        try {
            ServiceSocket testingServiceSocket = serviceSocketList.get(0);
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + applicationName + "/" + resourceName + "/" + testingServiceSocket.getId())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(testingServiceSocket.toJson());
            mockMvc.perform(request).andExpect(status().isOk());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void updateNotExistingResource() {
        try {
            ServiceSocket testingServiceSocket = serviceSocketList.get(0);
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + applicationName + "/" + resourceName + "/" + ObjectId.get().toString())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(testingServiceSocket.toJson());
            mockMvc.perform(request).andExpect(status().isNotFound());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void delete() {
        try {
            ServiceSocket testingServiceSocket = serviceSocketList.get(0);
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + applicationName + "/" + resourceName + "/" + testingServiceSocket.getId())
                    .contentType(MediaType.APPLICATION_JSON_UTF8);
            mockMvc.perform(request).andExpect(status().isOk());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void deleteNotExisting() {
        try {
            ServiceSocket testingServiceSocket = serviceSocketList.get(0);
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + applicationName + "/" + resourceName + "/" + ObjectId.get().toString())
                    .contentType(MediaType.APPLICATION_JSON_UTF8);
            mockMvc.perform(request).andExpect(status().isNotFound());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }
}
