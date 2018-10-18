package ru.majordomo.hms.rc.staff.test.api.http;

import org.bson.types.ObjectId;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
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

import ru.majordomo.hms.rc.staff.event.service.listener.ServiceMongoEventListener;
import ru.majordomo.hms.rc.staff.event.serviceTemplate.listener.ServiceTemplateMongoEventListener;
import ru.majordomo.hms.rc.staff.repositories.*;
import ru.majordomo.hms.rc.staff.resources.*;
import ru.majordomo.hms.rc.staff.test.config.*;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
                ServiceMongoEventListener.class,
                ServiceTemplateMongoEventListener.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ServiceRestControllerTest {
    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/generated-snippets");

    @Autowired
    private WebApplicationContext ctx;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private ServiceSocketRepository serviceSocketRepository;
    @Autowired
    private ServiceTemplateRepository serviceTemplateRepository;
    @Autowired
    private ConfigTemplateRepository configTemplateRepository;
    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    @Value("${spring.application.name}")
    private String applicationName;
    private String resourceName = "service";
    private List<Service> testServices = new ArrayList<>();
    private MockMvc mockMvc;

    private RestDocumentationResultHandler document;

    private static FieldDescriptor[] serviceFields = new FieldDescriptor[] {
            fieldWithPath("id").description("Service ID"),
            fieldWithPath("name").description("Имя Service"),
            fieldWithPath("switchedOn").description("Статус Service"),
            subsectionWithPath("serviceSockets").description("Список serviceSockets для Service"),
            fieldWithPath("serviceSocketIds").description("Список serviceSocketIds для Service"),
            subsectionWithPath("serviceTemplate").description("serviceTemplate для Service"),
            fieldWithPath("serviceTemplateId").description("serviceTemplateId для Service")
    };

    private void generateBatchOfServices() {
        for (int i = 2; i < 6; i++) {
            ServiceType serviceType = new ServiceType();
            switch (i) {
                case 1:
                    serviceType.setName("DATABASE_MYSQL");
                    break;
                case 2:
                    serviceType.setName("WEBSITE_APACHE_PHP53_HARDENED");
                    break;
                default:
                    serviceType.setName("DATABASE_POSTGRESQL");
                    break;
            }
            serviceTypeRepository.save(serviceType);

            // Создать сокет
            ServiceSocket serviceSocket = new ServiceSocket();
            serviceSocket.setAddress("10.10.10." + i);
            serviceSocket.setPort(2000 + i);
            serviceSocket.setName(serviceSocket.getAddressAsString() + ":" + serviceSocket.getPort());
            serviceSocketRepository.save(serviceSocket);

            ConfigTemplate configTemplate = new ConfigTemplate();
            configTemplateRepository.save(configTemplate);
            // Создать сервис темплейт
            ServiceTemplate serviceTemplate = new ServiceTemplate();
            serviceTemplate.setName("Шаблон сервиса " + i);
            serviceTemplate.addConfigTemplate(configTemplate);
            serviceTemplate.setServiceType(serviceType);
            serviceTemplateRepository.save(serviceTemplate);

            // Создать сервис и добавить в него сокет и сервис темплейт
            Service service = new Service();
            serviceTypeRepository.save(serviceType);

            service.setName("Сервис " + i);
            service.setSwitchedOn(Boolean.TRUE);
            service.setServiceTemplate(serviceTemplate);
            service.addServiceSocket(serviceSocket);

            serviceRepository.save(service);
            testServices.add(service);
        }
    }

    @Before
    public void setUp() {
        this.document = document("service/{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
        generateBatchOfServices();
    }

    @Test
    public void readOne() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/" + testServices.get(0).getId()).accept(MediaType.APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(this.document)
                    .andDo(this.document.document(responseFields(serviceFields)))
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
            mockMvc.perform(request).andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$").isArray())
                    .andDo(this.document)
                    .andDo(this.document.document(
                            responseFields(fieldWithPath("[]").description("Services"))
                                    .andWithPrefix("[].", serviceFields))
                    )
            ;
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAllByName() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/").param("name", testServices.get(2).getName()).accept(MediaType.APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$").isArray())
                    .andDo(this.document)
                    .andDo(this.document.document(
                            responseFields(fieldWithPath("[]").description("Services"))
                                    .andWithPrefix("[].", serviceFields))
                    )
            ;
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readOneAndCheckObjectFields() {
        Service testingService = testServices.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/" + testingService.getId()).accept(MediaType.APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(jsonPath("id").value(testingService.getId()))
                    .andExpect(jsonPath("id").value(testingService.getId()))
                    .andExpect(jsonPath("name").value(testingService.getName()))
                    .andExpect(jsonPath("switchedOn").value(testingService.getSwitchedOn()))
                    .andExpect(jsonPath("serviceTemplate.id").value(testingService.getServiceTemplateId()))
                    .andExpect(jsonPath("serviceSockets.[0].id").value(testingService.getServiceSocketIds().get(0))).andDo(print())
                    .andDo(this.document)
                    .andDo(this.document.document(responseFields(serviceFields)))
            ;
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void create() {
        Service service = testServices.get(0);
        service.setId(null);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/" + resourceName)
                                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                .content(service.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isCreated()).andDo(this.document);
         } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void update() {
        Service service = testServices.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + resourceName + "/" + service.getId())
                                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                .content(service.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isOk()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void updateNotExistingResource() {
        Service service = testServices.get(0);
        String unknownServiceId = ObjectId.get().toString();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + resourceName + "/" + unknownServiceId)
                                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                .content(service.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isNotFound()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void delete() {
        String serviceIdToDelete = testServices.get(0).getId();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + resourceName + "/" + serviceIdToDelete)
                                                .accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(status().isOk()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void deleteNotExisting() {
        String unknownServiceId = ObjectId.get().toString();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + resourceName + "/" + unknownServiceId);
        try {
            mockMvc.perform(request).andExpect(status().isNotFound()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void cleanAll() {
        serviceRepository.deleteAll();
        serviceTemplateRepository.deleteAll();
        serviceTypeRepository.deleteAll();
        serviceSocketRepository.deleteAll();
        configTemplateRepository.deleteAll();
    }
}
