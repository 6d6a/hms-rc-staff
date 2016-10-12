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

import ru.majordomo.hms.rc.staff.repositories.StorageRepository;
import ru.majordomo.hms.rc.staff.resources.Storage;
import ru.majordomo.hms.rc.staff.test.config.EmbeddedServltetContainerConfig;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.StorageServicesConfig;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoriesConfig.class, StorageServicesConfig.class, EmbeddedServltetContainerConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StorageRestControllerTest {
    @Autowired
    private StorageRepository repository;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private WebApplicationContext ctx;
    private MockMvc mockMvc;
    @Value("${spring.application.name}")
    private String applicationName;
    private String resourceName = "storage";
    private List<Storage> storages = new ArrayList<>();

    private void generateBatchOfStorages() {
        String namePattern = "Хранилище ";
        Boolean switchedOn = Boolean.TRUE;
        Double capacity = 5 * Math.pow(10, 9);
        Double used = 3 * Math.pow(10, 9);
        for (int i = 1; i < 5; i++) {
            Storage storage = new Storage();
            storage.setName(namePattern + i);
            storage.setSwitchedOn(switchedOn);
            storage.setCapacity(capacity);
            storage.setCapacityUsed(used);
            repository.save(storage);
            storages.add(storage);
        }
    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        generateBatchOfStorages();
    }

    @Test
    public void readOne() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName +
                "/" + resourceName + "/" + storages.get(0).getId()).accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF8"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAll() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName + "/" + resourceName + "/").accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF8"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readOneAndCheckObjectFields() {
        Storage testingStorage = storages.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName + "/" + resourceName + "/" + testingStorage.getId()).accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(jsonPath("name").value(testingStorage.getName()))
                    .andExpect(jsonPath("id").value(testingStorage.getId()))
                    .andExpect(jsonPath("switchedOn").value(testingStorage.getSwitchedOn()))
                    .andExpect(jsonPath("capacity").value(testingStorage.getCapacity()))
                    .andExpect(jsonPath("capacityUsed").value(testingStorage.getCapacityUsed()));
        } catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void create() {
        Storage testingStorage = storages.get(0);
        testingStorage.setId(null);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/" + applicationName
                + "/" + resourceName)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(testingStorage.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isCreated());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void update() {
        Storage storage = storages.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + applicationName
                + "/" + resourceName + "/" + storage.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(storage.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isOk());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void updateNotExistingResource() {
        Storage storage = storages.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + applicationName
                + "/" + resourceName + "/" + ObjectId.get().toString())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(storage.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isNotFound());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void delete() {
        Storage storage = storages.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + applicationName
                + "/" + resourceName + "/" + storage.getId())
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
