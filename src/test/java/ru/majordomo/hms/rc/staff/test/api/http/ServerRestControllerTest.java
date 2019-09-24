package ru.majordomo.hms.rc.staff.test.api.http;

import org.bson.types.ObjectId;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
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

import ru.majordomo.hms.rc.staff.event.server.listener.ServerMongoEventListener;
import ru.majordomo.hms.rc.staff.event.serverRole.listener.ServerRoleMongoEventListener;
import ru.majordomo.hms.rc.staff.event.service.listener.ServiceMongoEventListener;
import ru.majordomo.hms.rc.staff.event.serviceTemplate.listener.ServiceTemplateMongoEventListener;
import ru.majordomo.hms.rc.staff.repositories.*;
import ru.majordomo.hms.rc.staff.repositories.socket.SocketRepository;
import ru.majordomo.hms.rc.staff.repositories.template.TemplateRepository;
import ru.majordomo.hms.rc.staff.resources.*;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;
import ru.majordomo.hms.rc.staff.resources.socket.NetworkSocket;
import ru.majordomo.hms.rc.staff.resources.template.ApplicationServer;
import ru.majordomo.hms.rc.staff.resources.template.DatabaseServer;
import ru.majordomo.hms.rc.staff.resources.template.ResourceType;
import ru.majordomo.hms.rc.staff.test.config.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
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
                ServiceTemplateMongoEventListener.class,
                ServerMongoEventListener.class,
                ServiceMongoEventListener.class,
                ServerRoleMongoEventListener.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "server.active.name.shared-hosting:web99",
                "server.active.name.mail-storage:pop99",
                "server.active.name.mysql-database-server:mdb99",
                "server.active.mail-storage.active-storage-mountpoint:/homebig"
        }
)
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
    @Autowired
    private TemplateRepository templateRepository;
    @Autowired
    private SocketRepository socketRepository;

    private RestDocumentationResultHandler document;

    @Value("${server.active.name.shared-hosting}")
    private String activeSharedHostingName;

    @Value("${server.active.name.mail-storage}")
    private String activeMailStorageName;

    @Value("${server.active.name.mysql-database-server}")
    private String activeDatabaseServerName;

    @Value("${spring.application.name}")
    private String applicationName;

    private String resourceName = "server";
    private List<Server> testServers = new ArrayList<>();
    private MockMvc mockMvc;

    private static FieldDescriptor[] serverFields = new FieldDescriptor[] {
            fieldWithPath("id").description("Server ID"),
            fieldWithPath("name").description("Имя Server"),
            fieldWithPath("switchedOn").description("Статус Server"),
            subsectionWithPath("services").description("Список Service для Server"),
            fieldWithPath("serviceIds").description("Список serviceIds для Server"),
            subsectionWithPath("serverRoles").description("Список ServerRoles для Server"),
            fieldWithPath("serverRoleIds").description("Список serverRoleIds для Server"),
            subsectionWithPath("storages").description("Список Storages для Server"),
            fieldWithPath("storageIds").description("Список storageIds для Server")
    };

    private void generateBatchOfServers() {
        for (int i = 1; i < 10; i++) {

            ConfigTemplate configTemplate = new ConfigTemplate();
            configTemplateRepository.save(configTemplate);

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

            ApplicationServer applicationServer = new ApplicationServer();
            applicationServer.setName("php");
            applicationServer.addConfigTemplate(configTemplate);
            applicationServer.setAvailableToAccounts(true);
            applicationServer.setVersion("5.6");
            applicationServer.setSupervisionType("systemd");
            applicationServer.setLanguage(ApplicationServer.Language.PHP);
//            applicationServerTemplate.addProvidedResourceConfig(ResourceType.WEBSITE, new HashMap<>());
            templateRepository.save(applicationServer);

            DatabaseServer databaseServer = new DatabaseServer();
            databaseServer.setName("mysql");
            databaseServer.addConfigTemplate(configTemplate);
            databaseServer.setAvailableToAccounts(false);
            databaseServer.setSupervisionType("systemd");
            databaseServer.setType(DatabaseServer.Type.MYSQL);
//            databaseServer.addProvidedResourceConfig(ResourceType.DATABASE, new HashMap<>());
            templateRepository.save(databaseServer);

            ServiceTemplate serviceTemplate = new ServiceTemplate();
            serviceTemplate.addConfigTemplate(configTemplate);
            serviceTemplate.setServiceTypeName(serviceType.getName());
            serviceTemplateRepository.save(serviceTemplate);

            ServerRole serverRole = new ServerRole();
            ServerRole serverRole1 = new ServerRole();
            serverRole.addServiceTemplate(serviceTemplate);
            serverRole1.addServiceTemplate(serviceTemplate);
            switch (i) {
                case 1:
                    serverRole.setName("shared-hosting");
                    break;
                case 2:
                    serverRole.setName("mail-storage");
                    break;
                case 3:
                    serverRole.setName("mysql-database-server");
                    break;
                default:
                    serverRole.setName("Серверная роль " + i);
                    break;
            }
            serverRoleRepository.save(serverRole);
            serverRoleRepository.save(serverRole1);
            List<ServerRole> serverRoles = new ArrayList<>();
            serverRoles.add(serverRole);
            switch (i) {
                case 3:
                    serverRoles.add(serverRole1);
                    break;
                default:
                    break;
            }

            List<Service> services = new ArrayList<>();

            NetworkSocket socket = new NetworkSocket();
            socket.setAddress("10.10.10.1");
            socket.setPort(2000);
            socket.setProtocol("http");
            socket.setName(socket.getAddressAsString() + ":" + socket.getPort());
            socketRepository.save(socket);

            Service service = new Service();
            service.setServerId(String.valueOf(i));
            service.setTemplate(applicationServer);
            service.addSocket(socket);
            serviceRepository.save(service);

            services.add(service);

            Service service2 = new Service();
            service2.setServerId(String.valueOf(i));
            service2.setTemplate(databaseServer);
            service2.addSocket(socket);
            serviceRepository.save(service2);

            services.add(service2);

            List<Storage> storages = new ArrayList<>();
            Storage storage = new Storage();
            storage.setServerId(String.valueOf(i));
            if (i == 2) storage.setMountPoint("/homebig");
            storages.add(storage);
            storageRepository.saveAll(storages);

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
            server.setId(String.valueOf(i));
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
            mockMvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                    .andDo(this.document)
                    .andDo(this.document.document(responseFields(serverFields)))
            ;
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
                            responseFields(fieldWithPath("[]").description("Servers"))
                                    .andWithPrefix("[].", serverFields)
                    ));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAllServicesByIdAndServiceTypeDatabase() {
        MultiValueMap<String, String> keyValue = new LinkedMultiValueMap<>();
        keyValue.set("service-type", "DATABASE");

        //Возвращает список объектов Service
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/" + testServers.get(0).getId() + "/services").params(keyValue).accept(APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(testServers.get(0).getServices().get(1).getId()))
                    .andExpect(jsonPath("$[0].name").value(testServers.get(0).getServices().get(1).getName()))
                    .andExpect(jsonPath("$[0].switchedOn").value(testServers.get(0).getServices().get(1).getSwitchedOn()))
                    .andExpect(jsonPath("$[0].template.id").value(testServers.get(0).getServices().get(1).getTemplateId()))
                    .andExpect(jsonPath("$[0].sockets.[0].id").value(testServers.get(0).getServices().get(1).getSocketIds().get(0))).andDo(print());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAllServicesByIdAndServiceTypeWebsite() {
        MultiValueMap<String, String> keyValue = new LinkedMultiValueMap<>();
        keyValue.set("service-type", "WEBSITE");

        //Возвращает список объектов Service
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/" + testServers.get(1).getId() + "/services").params(keyValue).accept(APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk())
                    .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(testServers.get(1).getServices().get(0).getId()))
                    .andExpect(jsonPath("$[0].name").value(testServers.get(1).getServices().get(0).getName()))
                    .andExpect(jsonPath("$[0].switchedOn").value(testServers.get(1).getServices().get(0).getSwitchedOn()))
                    .andExpect(jsonPath("$[0].template.id").value(testServers.get(1).getServices().get(0).getTemplateId()))
                    .andExpect(jsonPath("$[0].sockets.[0].id").value(testServers.get(1).getServices().get(0).getSocketIds().get(0))).andDo(print());
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
                            responseFields(fieldWithPath("[]").description("Servers"))
                                    .andWithPrefix("[].", serverFields)
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
        keyValue.set("server-role", "mysql-database-server");
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
    public void filterActiveStorage() throws Exception {
        Server serverThatWeSearch = testServers.get(1);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/" + serverThatWeSearch.getId() + "/active-storage").accept(APPLICATION_JSON_UTF8);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("id").value(serverThatWeSearch.getStorages().get(0).getId()));
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
                    .andDo(this.document.document(responseFields(serverFields)))
            ;
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
