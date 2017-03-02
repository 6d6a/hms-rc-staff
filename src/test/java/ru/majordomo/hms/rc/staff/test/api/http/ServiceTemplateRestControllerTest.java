package ru.majordomo.hms.rc.staff.test.api.http;

import org.bson.types.ObjectId;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
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
import ru.majordomo.hms.rc.staff.repositories.ServiceTypeRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceType;
import ru.majordomo.hms.rc.staff.test.config.*;

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
                ConfigOfGovernors.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ServiceTemplateRestControllerTest {
    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/generated-snippets");

    @Autowired
    private ServiceTemplateRepository serviceTempalteRepository;
    @Autowired
    private ConfigTemplateRepository configTemplateRepository;
    @Autowired
    private WebApplicationContext ctx;
    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    private RestDocumentationResultHandler document;

    @Value("${spring.application.name}")
    private String applicationName;
    private String resourceName = "service-template";
    private MockMvc mockMvc;
    private List<ServiceTemplate> serviceTemplates = new ArrayList<>();

    private void generateBatchOfServiceTemplates() {
        String namePattern = "Шаблон для сервиса ";
        Boolean switchedOn = Boolean.TRUE;
        for (int i = 1; i < 6; i++) {
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

            String name = namePattern + i;
            ConfigTemplate configTemplate = new ConfigTemplate();
            configTemplate.setName("Шаблон конфигурационного файла " + i);
            configTemplate.setSwitchedOn(true);
            configTemplate.setFileLink("http://storage/" + ObjectId.get().toString());
            configTemplateRepository.save(configTemplate);
            ServiceTemplate serviceTemplate = new ServiceTemplate();
            serviceTemplate.setName(name);
            serviceTemplate.setSwitchedOn(switchedOn);
            serviceTemplate.addConfigTemplate(configTemplate);
            serviceTemplate.setServiceType(serviceType);
            serviceTempalteRepository.save(serviceTemplate);
            serviceTemplates.add(serviceTemplate);
        }
    }

    @Before
    public void setUp() {
        this.document = document("service-template/{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
        serviceTempalteRepository.deleteAll();
        configTemplateRepository.deleteAll();
        generateBatchOfServiceTemplates();
    }

    @Test
    public void readOne() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName +
                "/" + serviceTemplates.get(0).getId())
                .accept(MediaType.APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(this.document)
                    .andDo(this.document.document(
                            responseFields(
                                    fieldWithPath("id").description("ServiceTemplate ID"),
                                    fieldWithPath("name").description("Имя ServiceTemplate"),
                                    fieldWithPath("switchedOn").description("Статус ServiceTemplate"),
                                    fieldWithPath("configTemplates").description("Список СonfigTemplates для ServiceTemplate"),
                                    fieldWithPath("serviceType").description("ServiceType, к которому относится данный ServiceTemplate")
                            )
                    ));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAll() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/"
                + resourceName + "/").accept(MediaType.APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(this.document)
                    .andDo(this.document.document(
                            responseFields(
                                    fieldWithPath("[].id").description("ServiceTemplate ID"),
                                    fieldWithPath("[].name").description("Имя ServiceTemplate"),
                                    fieldWithPath("[].switchedOn").description("Статус ServiceTemplate"),
                                    fieldWithPath("[].configTemplates").description("Список СonfigTemplates для ServiceTemplate"),
                                    fieldWithPath("[].serviceType").description("ServiceType, к которому относится данный ServiceTemplate")
                            )
                    ));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAllByName() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/"
                + resourceName + "/").param("name", serviceTemplates.get(2).getName()).accept(MediaType.APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(this.document)
                    .andDo(this.document.document(
                            responseFields(
                                    fieldWithPath("[].id").description("ServiceTemplate ID"),
                                    fieldWithPath("[].name").description("Имя ServiceTemplate"),
                                    fieldWithPath("[].switchedOn").description("Статус ServiceTemplate"),
                                    fieldWithPath("[].configTemplates").description("Список СonfigTemplates для ServiceTemplate"),
                                    fieldWithPath("[].serviceType").description("ServiceType, к которому относится данный ServiceTemplate")
                            )
                    ));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readOneAndCheckObjectFields() {
        ServiceTemplate serviceTemplate = serviceTemplates.get(0);
        String testedServiceTemplateId = serviceTemplate.getId();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/" + testedServiceTemplateId)
                .accept(MediaType.APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(jsonPath("name").value(serviceTemplate.getName()))
                    .andExpect(jsonPath("id").value(serviceTemplate.getId()))
                    .andExpect(jsonPath("switchedOn").value(serviceTemplate.getSwitchedOn()))
                    .andExpect(jsonPath("configTemplates.[0].id").value(serviceTemplate.getConfigTemplateIds().get(0)))
                    .andDo(this.document)
                    .andDo(this.document.document(
                            responseFields(
                                    fieldWithPath("id").description("ServiceTemplate ID"),
                                    fieldWithPath("name").description("Имя ServiceTemplate"),
                                    fieldWithPath("switchedOn").description("Статус ServiceTemplate"),
                                    fieldWithPath("configTemplates").description("Список СonfigTemplates для ServiceTemplate"),
                                    fieldWithPath("serviceType").description("ServiceType, к которому относится данный ServiceTemplate")
                            )
                    ));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void create() {
        ServiceTemplate serviceTemplate = serviceTemplates.get(0);
        serviceTemplate.setId(null);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/" + resourceName + "/")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(serviceTemplate.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isCreated()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void update() {
        ServiceTemplate serviceTemplate = serviceTemplates.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + resourceName + "/" + serviceTemplate.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(serviceTemplate.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isOk()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void updateNotExistingResource() {
        ServiceTemplate serviceTemplate = serviceTemplates.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + resourceName + "/" + ObjectId.get().toString())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(serviceTemplate.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isNotFound()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void delete() {
        ServiceTemplate serviceTemplate = serviceTemplates.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + resourceName + "/" + serviceTemplate.getId())
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
        String unknownServiceTemplateId = ObjectId.get().toString();
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + resourceName + "/" + unknownServiceTemplateId)
                .accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(status().isNotFound()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void cleanAll() {
        serviceTempalteRepository.deleteAll();
    }
}
