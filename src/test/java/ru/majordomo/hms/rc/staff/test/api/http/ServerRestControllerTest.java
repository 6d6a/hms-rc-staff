package ru.majordomo.hms.rc.staff.test.api.http;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
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
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import ru.majordomo.hms.rc.staff.repositories.*;
import ru.majordomo.hms.rc.staff.resources.*;
import ru.majordomo.hms.rc.staff.test.config.EmbeddedServltetContainerConfig;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.ServerServicesConfig;

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
@SpringBootTest(classes = {RepositoriesConfig.class, EmbeddedServltetContainerConfig.class, ServerServicesConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

    private RestDocumentationResultHandler document;

    @Value("${spring.application.name}")
    private String applicationName;
    private String resourceName = "server";
    private List<Server> testServers = new ArrayList<>();
    private MockMvc mockMvc;

    private void generateBatchOfServers() {
        for (int i = 1; i < 6; i++) {

            ConfigTemplate configTemplate = new ConfigTemplate();
            configTemplateRepository.save(configTemplate);

            ServiceTemplate serviceTemplate = new ServiceTemplate();
            serviceTemplate.addConfigTemplate(configTemplate);
            serviceTemplateRepository.save(serviceTemplate);

            ServerRole serverRole = new ServerRole();
            serverRole.addServiceTemplate(serviceTemplate);
            serverRoleRepository.save(serverRole);

            List<Service> services = new ArrayList<>();
            Service service = new Service();
            ServiceSocket serviceSocket = new ServiceSocket();

            serviceSocketRepository.save(serviceSocket);
            service.setServiceTemplate(serviceTemplate);
            service.addServiceSocket(serviceSocket);
            services.add(service);
            serviceRepository.save(services);

            List<Storage> storages = new ArrayList<>();
            Storage storage = new Storage();
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
        this.document = document("server/{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
        generateBatchOfServers();
    }

    @Test
    public void readOne() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName +
            "/" + resourceName + "/" + testServers.get(0).getId()).accept(MediaType.APPLICATION_JSON_UTF8);

        this.document.snippets(
                responseFields(
                        fieldWithPath("id").description("Server ID"),
                        fieldWithPath("name").description("Имя Server"),
                        fieldWithPath("switchedOn").description("Статус Server"),
                        fieldWithPath("services").description("Список Service для Server"),
                        fieldWithPath("serverRole").description("ServerRole для Server"),
                        fieldWithPath("storages").description("Список Storages для Server")
                )
        );

        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAll() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName +
                "/" + resourceName ).accept(MediaType.APPLICATION_JSON_UTF8);

        this.document.snippets(
                responseFields(
                        fieldWithPath("[].id").description("Server ID"),
                        fieldWithPath("[].name").description("Имя Server"),
                        fieldWithPath("[].switchedOn").description("Статус Server"),
                        fieldWithPath("[].services").description("Список Service для Server"),
                        fieldWithPath("[].serverRole").description("ServerRole для Server"),
                        fieldWithPath("[].storages").description("Список Storages для Server")
                )
        );

        try {
            mockMvc.perform(request).andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("$").isArray())
                    .andDo(this.document);
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

        this.document.snippets(
                responseFields(
                        fieldWithPath("id").description("Server ID"),
                        fieldWithPath("name").description("Имя Server"),
                        fieldWithPath("switchedOn").description("Статус Server"),
                        fieldWithPath("services").description("Список Service для Server"),
                        fieldWithPath("serverRole").description("ServerRole для Server"),
                        fieldWithPath("storages").description("Список Storages для Server")
                )
        );

        try {
            mockMvc.perform(request).andExpect(jsonPath("id").value(testingServer.getId()))
                    .andExpect(jsonPath("name").value(testingServer.getName()))
                    .andExpect(jsonPath("switchedOn").value(testingServer.getSwitchedOn()))
                    .andExpect(jsonPath("serverRole.id").value(testingServer.getServerRoleId()))
                    .andExpect(jsonPath("services").isArray())
                    .andExpect(jsonPath("services.[0].id").value(testingServer.getServices().get(0).getId()))
                    .andExpect(jsonPath("storages").isArray())
                    .andExpect(jsonPath("storages.[0].id").value(testingServer.getStorages().get(0).getId()))
                    .andDo(this.document);
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
            mockMvc.perform(request).andExpect(status().isCreated()).andDo(this.document);
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
            mockMvc.perform(request).andExpect(status().isOk()).andDo(this.document);
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
            mockMvc.perform(request).andExpect(status().isNotFound()).andDo(this.document);
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
            mockMvc.perform(request).andExpect(status().isOk()).andDo(this.document);
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
            mockMvc.perform(request).andExpect(status().isNotFound()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
