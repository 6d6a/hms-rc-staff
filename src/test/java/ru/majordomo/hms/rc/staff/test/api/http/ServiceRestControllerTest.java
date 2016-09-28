package ru.majordomo.hms.rc.staff.test.api.http;

import net.minidev.json.JSONArray;

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

import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;
import ru.majordomo.hms.rc.staff.test.config.EmbeddedServltetContainerConfig;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.ServiceServicesConfig;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoriesConfig.class, EmbeddedServltetContainerConfig.class, ServiceServicesConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ServiceRestControllerTest {

    @Autowired
    WebApplicationContext ctx;
    @Autowired
    private ServiceRepository repository;

    @Value("${spring.application.name}")
    private String applicationName;
    private String resourceName = "service";
    private List<Service> testServiceList = new ArrayList<>();
    private MockMvc mockMvc;

    private void generateBatchOfServices() {
        for (int i = 2; i < 6; i++) {
            // Создать сокет
            ServiceSocket serviceSocket = new ServiceSocket();
            serviceSocket.setAddress("10.10.10." + i);
            serviceSocket.setPort(2000 + i);
            serviceSocket.setName(serviceSocket.getAddressAsString() + ":" + serviceSocket.getPort());
            serviceSocket.setId(ObjectId.get().toString());

            // Создать сервис темплейт
            ServiceTemplate serviceTemplate = new ServiceTemplate();
            serviceTemplate.setName("Шаблон сервиса " + i);
            serviceTemplate.setId(ObjectId.get().toString());

            // Создать сервис и добавить в него сокет и сервис темплейт
            Service service = new Service();
            service.setName("Сервис " + i);
            service.setSwitchedOn(Boolean.TRUE);
            service.setServiceTemplate(serviceTemplate);
            service.addServiceSocket(serviceSocket);

            repository.save(service);
            testServiceList.add(service);
        }
    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        generateBatchOfServices();
    }

    @Test
    public void readOne() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName +
            "/" + resourceName + "/" + testServiceList.get(0).getId()).accept(MediaType.APPLICATION_JSON_UTF8);
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
            "/" + resourceName + "/").accept(MediaType.APPLICATION_JSON_UTF8);
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
        Service testingService = testServiceList.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName +
            "/" + resourceName + "/" + testingService.getId()).accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(jsonPath("id").value(testingService.getId()))
                    .andExpect(jsonPath("id").value(testingService.getId()))
                    .andExpect(jsonPath("name").value(testingService.getName()))
                    .andExpect(jsonPath("switchedOn").value(testingService.getSwitchedOn()))
                    .andExpect(jsonPath("serviceTemplate").value(testingService.getServiceTemplateId()))
                    .andExpect(jsonPath("serviceSocketList.[0]").value(testingService.getServiceSocketIdList().get(0)));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
