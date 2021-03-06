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
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import ru.majordomo.hms.rc.staff.repositories.StorageRepository;
import ru.majordomo.hms.rc.staff.resources.Storage;
import ru.majordomo.hms.rc.staff.test.config.*;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                RepositoriesConfig.class,
                ConfigOfRestControllers.class,
                ConfigOfGovernors.class,
                ValidationConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class StorageRestControllerTest {
    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/generated-snippets");

    @Autowired
    private StorageRepository repository;

    @Autowired
    private WebApplicationContext ctx;

    private MockMvc mockMvc;

    @Value("${spring.application.name}")
    private String applicationName;

    private String resourceName = "storage";

    private List<Storage> storages = new ArrayList<>();

    private RestDocumentationResultHandler document;

    private static FieldDescriptor[] storageFields = new FieldDescriptor[] {
            fieldWithPath("id").description("Storage ID"),
            fieldWithPath("serverId").description("Server ID"),
            fieldWithPath("name").description("?????? Storage"),
            fieldWithPath("switchedOn").description("???????????? Storage"),
            fieldWithPath("capacity").description("?????????? ??????????????????"),
            fieldWithPath("capacityUsed").description("?????????????? ?????????? ??????????????????"),
            fieldWithPath("mountPoint").description("mountPoint ??????????????????")
    };

    private void generateBatchOfStorages() {
        String namePattern = "?????????????????? ";
        Boolean switchedOn = Boolean.TRUE;
        Double capacity = 5 * Math.pow(10, 9);
        Double used = 3 * Math.pow(10, 9);
        for (int i = 1; i < 5; i++) {
            Storage storage = new Storage();
            storage.setName(namePattern + i);
            storage.setSwitchedOn(switchedOn);
            storage.setCapacity(capacity);
            storage.setCapacityUsed(used);
            storage.setMountPoint("/home/");
            repository.save(storage);
            storages.add(storage);
        }
    }

    @Before
    public void setUp() {
        this.document = document("storage/{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
        generateBatchOfStorages();
    }

    @Test
    public void readOne() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/" + storages.get(0).getId()).accept(MediaType.APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(this.document)
                    .andDo(this.document.document(responseFields(storageFields)))
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
                            responseFields(fieldWithPath("[]").description("Storages"))
                                    .andWithPrefix("[].", storageFields))
            );
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAllByName() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/").param("name", storages.get(2).getName()).accept(MediaType.APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(this.document)
                    .andDo(this.document.document(
                            responseFields(fieldWithPath("[]").description("Storages"))
                                    .andWithPrefix("[].", storageFields))
                    );
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readOneAndCheckObjectFields() {
        Storage testingStorage = storages.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/" + testingStorage.getId()).accept(MediaType.APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(jsonPath("name").value(testingStorage.getName()))
                    .andExpect(jsonPath("id").value(testingStorage.getId()))
                    .andExpect(jsonPath("switchedOn").value(testingStorage.getSwitchedOn()))
                    .andExpect(jsonPath("capacity").value(testingStorage.getCapacity()))
                    .andExpect(jsonPath("capacityUsed").value(testingStorage.getCapacityUsed()))
                    .andDo(this.document)
                    .andDo(this.document.document(responseFields(storageFields)))
            ;
        } catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void create()  throws Exception {
        Storage testingStorage = storages.get(0);
        testingStorage.setId(null);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/" + resourceName)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(testingStorage.toJson());
        mockMvc.perform(request).andExpect(status().isCreated()).andDo(this.document);
    }

    @Test
    public void update() throws Exception {
        Storage storage = storages.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + resourceName + "/" + storage.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(storage.toJson());
        mockMvc.perform(request).andExpect(status().isOk()).andDo(this.document);
    }

    @Test
    public void updateNotExistingResource() throws Exception {
        Storage storage = storages.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + resourceName + "/" + ObjectId.get().toString())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(storage.toJson());
        mockMvc.perform(request).andExpect(status().isNotFound()).andDo(this.document);
    }

    @Test
    public void delete() throws Exception {
        Storage storage = storages.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + resourceName + "/" + storage.getId())
                .accept(MediaType.APPLICATION_JSON_UTF8);
        mockMvc.perform(request).andExpect(status().isOk()).andDo(this.document);
    }

    @Test
    public void deleteNotExisting() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + resourceName + "/" + ObjectId.get().toString())
                .accept(MediaType.APPLICATION_JSON_UTF8);
        mockMvc.perform(request).andExpect(status().isNotFound()).andDo(this.document);
    }

    @After
    public void cleanAll() {
        repository.deleteAll();
    }
}
