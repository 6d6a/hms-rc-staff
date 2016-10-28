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

import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.test.config.ConfigTemplateServicesConfig;
import ru.majordomo.hms.rc.staff.test.config.EmbeddedServltetContainerConfig;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;

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
@SpringBootTest(classes = {RepositoriesConfig.class, ConfigTemplateServicesConfig.class, EmbeddedServltetContainerConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfigTemplateRestControllerTest {
    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/generated-snippets");

    @Autowired
    private ConfigTemplateRepository configTemplateRepository;
    @Autowired
    private WebApplicationContext ctx;
    @Value("${spring.application.name}")
    private String applicationName;
    private String resourceName = "config-template";

    private MockMvc mockMvc;
    private List<ConfigTemplate> configTemplates = new ArrayList<>();

    private RestDocumentationResultHandler document;

    private void generateBatchOfConfigTemplates() {
        String namePattern = "Шаблон ";
        Boolean switchedOn = Boolean.TRUE;
        String fileLinkPattern = "http://file-service/file/";
        for (int i = 0; i < 9; i++) {
            ConfigTemplate configTemplate = new ConfigTemplate();
            configTemplate.setName(namePattern + i);
            configTemplate.setSwitchedOn(switchedOn);
            configTemplate.setFileLink(fileLinkPattern + ObjectId.get().toString());
            configTemplateRepository.save(configTemplate);
            configTemplates.add(configTemplate);
        }
    }

    @Before
    public void setUp() {
        this.document = document("config-template/{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
        mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
        configTemplateRepository.deleteAll();
        generateBatchOfConfigTemplates();
    }

    @Test
    public void readOne() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/" + configTemplates.get(0).getId())
                .accept(MediaType.APPLICATION_JSON_UTF8);

        this.document.snippets(
                responseFields(
                        fieldWithPath("id").description("ConfigTemplate ID"),
                        fieldWithPath("name").description("Имя ConfigTemplate"),
                        fieldWithPath("switchedOn").description("Статус ConfigTemplate"),
                        fieldWithPath("fileLink").description("Ссылка на файл")
                )
        );

        try {
            mockMvc.perform(request).andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAll() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/").accept(MediaType.APPLICATION_JSON_UTF8);

        this.document.snippets(
                responseFields(
                        fieldWithPath("[].id").description("ConfigTemplate ID"),
                        fieldWithPath("[].name").description("Имя ConfigTemplate"),
                        fieldWithPath("[].switchedOn").description("Статус ConfigTemplate"),
                        fieldWithPath("[].fileLink").description("Ссылка на файл")
                )
        );

        try {
            mockMvc.perform(request).andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readOneAndCheckObjectFields() {
        ConfigTemplate configTemplate = configTemplates.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/" + configTemplate.getId()).accept(MediaType.APPLICATION_JSON_UTF8);

        this.document.snippets(
                responseFields(
                        fieldWithPath("id").description("ConfigTemplate ID"),
                        fieldWithPath("name").description("Имя ConfigTemplate"),
                        fieldWithPath("switchedOn").description("Статус ConfigTemplate"),
                        fieldWithPath("fileLink").description("Ссылка на файл")
                )
        );

        try {
            mockMvc.perform(request).andExpect(jsonPath("name").value(configTemplate.getName()))
                    .andExpect(jsonPath("switchedOn").value(configTemplate.getSwitchedOn()))
                    .andExpect(jsonPath("fileLink").value(configTemplate.getFileLink()))
                    .andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void create() {
        ConfigTemplate configTemplate = configTemplates.get(0);
        configTemplate.setId(null);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/" + resourceName)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(configTemplate.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isCreated()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void update() {
        ConfigTemplate configTemplate = configTemplates.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + resourceName + "/" + configTemplate.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(configTemplate.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isOk()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void updateNotExistingResource() {
        ConfigTemplate configTemplate = configTemplates.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + resourceName + "/" + ObjectId.get().toString())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(configTemplate.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isNotFound()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void delete() {
        ConfigTemplate configTemplate = configTemplates.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + resourceName + "/" + configTemplate.getId())
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
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + resourceName + "/" + ObjectId.get().toString())
                .accept(MediaType.APPLICATION_JSON_UTF8);
        try {
            mockMvc.perform(request).andExpect(status().isNotFound()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
