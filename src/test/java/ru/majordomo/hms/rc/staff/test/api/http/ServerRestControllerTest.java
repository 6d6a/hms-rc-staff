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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.majordomo.hms.rc.staff.repositories.*;
import ru.majordomo.hms.rc.staff.resources.*;
import ru.majordomo.hms.rc.staff.test.config.EmbeddedServltetContainerConfig;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.ServerServicesConfig;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoriesConfig.class, EmbeddedServltetContainerConfig.class, ServerServicesConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"server.active.name.shared-hosting:web99", "server.active.name.mail-storage:pop99", "server.active.name.database-server:mdb99"})
public class ServerRestControllerTest {
    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/generated-snippets");

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
    @Autowired
    private ServiceTemplateRepository serviceTemplateRepository;
    @Autowired
    private ConfigTemplateRepository configTemplateRepository;
    @Autowired
    private ServiceSocketRepository serviceSocketRepository;
    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    private RestDocumentationResultHandler document;

    @Value("${server.active.name.shared-hosting}")
    private String activeSharedHostingName;

    @Value("${server.active.name.mail-storage}")
    private String activeMailStorageName;

    @Value("${server.active.name.database-server}")
    private String activeDatabaseServerName;

    @Value("${spring.application.name}")
    private String applicationName;

    private String resourceName = "server";
    private List<Server> testServers = new ArrayList<>();
    private MockMvc mockMvc;

    private void generateBatchOfServers() {
        for (int i = 1; i < 10; i++) {

            ConfigTemplate configTemplate = new ConfigTemplate();
            configTemplateRepository.save(configTemplate);

            ServiceTemplate serviceTemplate = new ServiceTemplate();
            serviceTemplate.addConfigTemplate(configTemplate);
            serviceTemplateRepository.save(serviceTemplate);

            ServerRole serverRole = new ServerRole();
            serverRole.addServiceTemplate(serviceTemplate);
            switch (i) {
                case 1:
                    serverRole.setName("shared-hosting");
                    break;
                case 2:
                    serverRole.setName("mail-storage");
                    break;
                case 3:
                    serverRole.setName("database-server");
                    break;
                default:
                    serverRole.setName("Серверная роль " + i);
                    break;
            }
            serverRoleRepository.save(serverRole);
            List<ServerRole> serverRoles = new ArrayList<>();
            serverRoles.add(serverRole);

            List<Service> services = new ArrayList<>();
            Service service = new Service();
            ServiceSocket serviceSocket = new ServiceSocket();
            serviceSocketRepository.save(serviceSocket);

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
            service.setServiceType(serviceType);
            service.setServiceTemplate(serviceTemplate);
            service.addServiceSocket(serviceSocket);
            services.add(service);
            serviceRepository.save(services);

            List<Storage> storages = new ArrayList<>();
            Storage storage = new Storage();
            storages.add(storage);
            storageRepository.save(storages);

            String name;
            switch (i) {
                case 1:
                    name = activeSharedHostingName;
                    break;
                case 2:
                    name = activeMailStorageName;
                    break;
                case 3:
                    name = activeDatabaseServerName;
                    break;
                default:
                    name = "Сервер " + i;
                    break;
            }
            Boolean switchedOn = Boolean.TRUE;

            Server server = new Server();
            server.setName(name);
            server.setSwitchedOn(switchedOn);
            server.setServerRoles(serverRoles);
            server.setServices(services);
            server.setStorages(storages);

            serverRepository.save(server);
            testServers.add(server);
        }
    }

    @Before
    public void setUp() {
        this.document = document("server/{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
        generateBatchOfServers();
    }

    @Test
    public void readOne() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/" + testServers.get(0).getId()).accept(APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON_UTF8))
                    .andDo(this.document)
                    .andDo(this.document.document(
                    responseFields(
                            fieldWithPath("id").description("Server ID"),
                            fieldWithPath("name").description("Имя Server"),
                            fieldWithPath("switchedOn").description("Статус Server"),
                            fieldWithPath("services").description("Список Service для Server"),
                            fieldWithPath("serverRoles").description("Список ServerRoles для Server"),
                            fieldWithPath("storages").description("Список Storages для Server")
                    )
            ));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAll() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName ).accept(APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$").isArray())
                    .andDo(this.document)
                    .andDo(this.document.document(
                            responseFields(
                                    fieldWithPath("[].id").description("Server ID"),
                                    fieldWithPath("[].name").description("Имя Server"),
                                    fieldWithPath("[].switchedOn").description("Статус Server"),
                                    fieldWithPath("[].services").description("Список Service для Server"),
                                    fieldWithPath("[].serverRoles").description("Список ServerRoles для Server"),
                                    fieldWithPath("[].storages").description("Список Storages для Server")
                            )
                    ));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAllServicesByIdAndServiceTypeDatabase() {
        MultiValueMap<String, String> keyValue = new LinkedMultiValueMap<>();
        keyValue.set("service-type", "database");

        //Возвращает список объектов Service
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/" + testServers.get(0).getId() + "/services").params(keyValue).accept(APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(testServers.get(0).getServices().get(0).getId()))
                    .andExpect(jsonPath("$[0].name").value(testServers.get(0).getServices().get(0).getName()))
                    .andExpect(jsonPath("$[0].switchedOn").value(testServers.get(0).getServices().get(0).getSwitchedOn()))
                    .andExpect(jsonPath("$[0].serviceTemplate.id").value(testServers.get(0).getServices().get(0).getServiceTemplateId()))
                    .andExpect(jsonPath("$[0].serviceSockets.[0].id").value(testServers.get(0).getServices().get(0).getServiceSocketIds().get(0))).andDo(print())
                    .andExpect(jsonPath("$[0].serviceType.name").value(testServers.get(0).getServices().get(0).getServiceType().getName()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAllServicesByIdAndServiceTypeWebsite() {
        MultiValueMap<String, String> keyValue = new LinkedMultiValueMap<>();
        keyValue.set("service-type", "website");

        //Возвращает список объектов Service
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/" + testServers.get(1).getId() + "/services").params(keyValue).accept(APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(testServers.get(1).getServices().get(0).getId()))
                    .andExpect(jsonPath("$[0].name").value(testServers.get(1).getServices().get(0).getName()))
                    .andExpect(jsonPath("$[0].switchedOn").value(testServers.get(1).getServices().get(0).getSwitchedOn()))
                    .andExpect(jsonPath("$[0].serviceTemplate.id").value(testServers.get(1).getServices().get(0).getServiceTemplateId()))
                    .andExpect(jsonPath("$[0].serviceSockets.[0].id").value(testServers.get(1).getServices().get(0).getServiceSocketIds().get(0))).andDo(print())
                    .andExpect(jsonPath("$[0].serviceType.name").value(testServers.get(1).getServices().get(0).getServiceType().getName()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAllByName() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName ).param("name", testServers.get(2).getName()).accept(APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$").isArray())
                    .andDo(this.document)
                    .andDo(this.document.document(
                            responseFields(
                                    fieldWithPath("[].id").description("Server ID"),
                                    fieldWithPath("[].name").description("Имя Server"),
                                    fieldWithPath("[].switchedOn").description("Статус Server"),
                                    fieldWithPath("[].services").description("Список Service для Server"),
                                    fieldWithPath("[].serverRoles").description("Список ServerRoles для Server"),
                                    fieldWithPath("[].storages").description("Список Storages для Server")
                            )
                    ));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readByServerRoleIdAndActiveSharedHosting() {
        MultiValueMap<String, String> keyValue = new LinkedMultiValueMap<>();
        keyValue.set("server-role", "shared-hosting");
        keyValue.set("state", "active");

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/filter" ).params(keyValue).accept(APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("name").value(testServers.get(0).getName()))
                    .andExpect(jsonPath("name").value(activeSharedHostingName))
                    .andExpect(jsonPath("switchedOn").value(testServers.get(0).getSwitchedOn()))
                    .andExpect(jsonPath("serverRoles").isArray())
                    .andExpect(jsonPath("serverRoles.[0].id").value(testServers.get(0).getServerRoles().get(0).getId()))
                    .andExpect(jsonPath("services").isArray())
                    .andExpect(jsonPath("services.[0].id").value(testServers.get(0).getServices().get(0).getId()))
                    .andExpect(jsonPath("storages").isArray())
                    .andExpect(jsonPath("storages.[0].id").value(testServers.get(0).getStorages().get(0).getId()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readByServerRoleIdAndActiveMailStorage() {
        MultiValueMap<String, String> keyValue = new LinkedMultiValueMap<>();
        keyValue.set("server-role", "mail-storage");
        keyValue.set("state", "active");

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/filter" ).params(keyValue).accept(APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("name").value(testServers.get(1).getName()))
                    .andExpect(jsonPath("name").value(activeMailStorageName))
                    .andExpect(jsonPath("switchedOn").value(testServers.get(1).getSwitchedOn()))
                    .andExpect(jsonPath("serverRoles").isArray())
                    .andExpect(jsonPath("serverRoles.[0].id").value(testServers.get(1).getServerRoles().get(0).getId()))
                    .andExpect(jsonPath("services").isArray())
                    .andExpect(jsonPath("services.[0].id").value(testServers.get(1).getServices().get(0).getId()))
                    .andExpect(jsonPath("storages").isArray())
                    .andExpect(jsonPath("storages.[0].id").value(testServers.get(1).getStorages().get(0).getId()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readByServerRoleIdAndActiveDatabaseServer() {
        MultiValueMap<String, String> keyValue = new LinkedMultiValueMap<>();
        keyValue.set("server-role", "database-server");
        keyValue.set("state", "active");

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/filter" ).params(keyValue).accept(APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("name").value(testServers.get(2).getName()))
                    .andExpect(jsonPath("name").value(activeDatabaseServerName))
                    .andExpect(jsonPath("switchedOn").value(testServers.get(2).getSwitchedOn()))
                    .andExpect(jsonPath("serverRoles").isArray())
                    .andExpect(jsonPath("serverRoles.[0].id").value(testServers.get(2).getServerRoles().get(0).getId()))
                    .andExpect(jsonPath("services").isArray())
                    .andExpect(jsonPath("services.[0].id").value(testServers.get(2).getServices().get(0).getId()))
                    .andExpect(jsonPath("storages").isArray())
                    .andExpect(jsonPath("storages.[0].id").value(testServers.get(2).getStorages().get(0).getId()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readByServiceId() throws Exception {
        Server serverThatWeSearch = testServers.get(0);

        MultiValueMap<String, String> keyValue = new LinkedMultiValueMap<>();
        keyValue.set("service-id", serverThatWeSearch.getServiceIds().get(0));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/filter").params(keyValue).accept(APPLICATION_JSON_UTF8);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("id").value(serverThatWeSearch.getId()));
    }

    @Test
    public void readOneAndCheckObjectFields() {
        Server testingServer = testServers.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/" + testingServer.getId()).accept(APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(jsonPath("id").value(testingServer.getId()))
                    .andExpect(jsonPath("name").value(testingServer.getName()))
                    .andExpect(jsonPath("switchedOn").value(testingServer.getSwitchedOn()))
                    .andExpect(jsonPath("serverRoles").isArray())
                    .andExpect(jsonPath("serverRoles.[0].id").value(testingServer.getServerRoles().get(0).getId()))
                    .andExpect(jsonPath("services").isArray())
                    .andExpect(jsonPath("services.[0].id").value(testingServer.getServices().get(0).getId()))
                    .andExpect(jsonPath("storages").isArray())
                    .andExpect(jsonPath("storages.[0].id").value(testingServer.getStorages().get(0).getId()))
                    .andDo(this.document)
                    .andDo(this.document.document(
                            responseFields(
                                    fieldWithPath("id").description("Server ID"),
                                    fieldWithPath("name").description("Имя Server"),
                                    fieldWithPath("switchedOn").description("Статус Server"),
                                    fieldWithPath("services").description("Список Service для Server"),
                                    fieldWithPath("serverRoles").description("Список ServerRoles для Server"),
                                    fieldWithPath("storages").description("Список Storages для Server")
                            )
                    ));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void create() {
        Server server = testServers.get(0);
        server.setId(null);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/" + resourceName)
                .contentType(APPLICATION_JSON_UTF8)
                .content(server.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isCreated()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void update() {
        Server server = testServers.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + resourceName + "/" + server.getId())
                .contentType(APPLICATION_JSON_UTF8)
                .content(server.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isOk()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void updateNotExistingResource() {
        Server server = testServers.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + resourceName + "/" + ObjectId.get().toString())
                .contentType(APPLICATION_JSON_UTF8)
                .content(server.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isNotFound()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void delete() {
        Server server = testServers.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + resourceName + "/" + server.getId())
                .accept(APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(status().isOk()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void deleteNotExisting() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + resourceName + "/" + ObjectId.get().toString())
                .accept(APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(status().isNotFound()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void cleanAll() {
        serverRepository.deleteAll();
        configTemplateRepository.deleteAll();
        serviceTemplateRepository.deleteAll();
        serverRoleRepository.deleteAll();
        serviceSocketRepository.deleteAll();
        serviceRepository.deleteAll();
    }
}
