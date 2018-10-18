package ru.majordomo.hms.rc.staff.test.api.http;

import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import ru.majordomo.hms.rc.staff.exception.handler.RestResponseEntityExceptionHandler;
import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceSocketRepository;
import ru.majordomo.hms.rc.staff.resources.Network;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;
import ru.majordomo.hms.rc.staff.test.config.ConfigOfGovernors;
import ru.majordomo.hms.rc.staff.test.config.ConfigOfRestControllers;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.ValidationConfig;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                RepositoriesConfig.class,
                ConfigOfRestControllers.class,
                ConfigOfGovernors.class,
                ValidationConfig.class,
                RestResponseEntityExceptionHandler.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ServiceSocketRestControllerTest {
    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/generated-snippets");

    @Autowired
    private ServiceSocketRepository serviceSocketRepository;
    @Autowired
    private NetworkRepository networkRepository;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private WebApplicationContext ctx;
    @Value("${spring.application.name}")
    private String applicationName;
    private String resourceName = "service-socket";

    private RestDocumentationResultHandler document;

    private MockMvc mockMvc;
    private List<ServiceSocket> serviceSocketList = new ArrayList<>();

    private static FieldDescriptor[] serviceSocketFields = new FieldDescriptor[] {
            fieldWithPath("id").description("ServiceSocket ID"),
            fieldWithPath("name").description("Имя ServiceSocket"),
            fieldWithPath("switchedOn").description("Статус ServiceSocket"),
            fieldWithPath("port").description("Порт"),
            fieldWithPath("address").description("IP")
    };

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

            serviceSocketRepository.save(serviceSocket);
            serviceSocketList.add(serviceSocket);
        }
    }

    @Before
    public void setUp() {
        this.document = document("service-socket/{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
        serviceSocketRepository.deleteAll();
        networkRepository.deleteAll();
        generateBatchOfSockets();
    }

    @Test
    public void readOne() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/" + serviceSocketList.get(0).getId()).accept(MediaType.APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(this.document)
                    .andDo(this.document.document(responseFields(serviceSocketFields)))
            ;
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAll() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/").accept(MediaType.APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(this.document)
                    .andDo(this.document.document(
                            responseFields(fieldWithPath("[]").description("ServiceSockets"))
                                    .andWithPrefix("[].", serviceSocketFields))
            );
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAllByName() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/").param("name", serviceSocketList.get(2).getName()).accept(MediaType.APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(this.document)
                    .andDo(this.document.document(
                            responseFields(fieldWithPath("[]").description("ServiceSockets"))
                                    .andWithPrefix("[].", serviceSocketFields))
                    );
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readOneAndCheckObjectFields() {

        try {
            ServiceSocket testingServiceSocket = serviceSocketList.get(0);
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/" + testingServiceSocket.getId()).accept(MediaType.APPLICATION_JSON_UTF8);

            mockMvc.perform(request).andExpect(jsonPath("port").value(testingServiceSocket.getPort()))
                    .andExpect(jsonPath("address").value(testingServiceSocket.getAddressAsString()))
                    .andExpect(jsonPath("switchedOn").value(testingServiceSocket.getSwitchedOn()))
                    .andExpect(jsonPath("name").value(testingServiceSocket.getName()))
                    .andExpect(jsonPath("id").value(testingServiceSocket.getId()))
                    .andDo(this.document)
                    .andDo(this.document.document(responseFields(serviceSocketFields)))
            ;
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
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/" + resourceName + "/")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(testingServiceSocket.toJson());
            mockMvc.perform(request).andExpect(status().isCreated()).andDo(this.document);

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
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/" + resourceName + "/")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(testingServiceSocket.toJson());
            mockMvc.perform(request).andExpect(status().isBadRequest()).andDo(this.document);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void update() {
        try {
            ServiceSocket testingServiceSocket = serviceSocketList.get(0);
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + resourceName + "/" + testingServiceSocket.getId())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(testingServiceSocket.toJson());
            mockMvc.perform(request).andExpect(status().isOk()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void updateNotExistingResource() {
        try {
            ServiceSocket testingServiceSocket = serviceSocketList.get(0);
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + resourceName + "/" + ObjectId.get().toString())
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .content(testingServiceSocket.toJson());
            mockMvc.perform(request).andExpect(status().isNotFound()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void delete() {
        try {
            ServiceSocket testingServiceSocket = serviceSocketList.get(0);
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + resourceName + "/" + testingServiceSocket.getId())
                    .contentType(MediaType.APPLICATION_JSON_UTF8);
            mockMvc.perform(request).andExpect(status().isOk()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void deleteNotExisting() {
        try {
            ServiceSocket testingServiceSocket = serviceSocketList.get(0);
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + resourceName + "/" + ObjectId.get().toString())
                    .contentType(MediaType.APPLICATION_JSON_UTF8);
            mockMvc.perform(request).andExpect(status().isNotFound()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @After
    public void cleanAll() {
        serviceSocketRepository.deleteAll();
    }
}
