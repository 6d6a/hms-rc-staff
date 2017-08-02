package ru.majordomo.hms.rc.staff.test.api.http;

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
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import ru.majordomo.hms.rc.staff.repositories.ServerIpInfoRepository;
import ru.majordomo.hms.rc.staff.resources.DTO.ServerIpInfo;
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
                ConfigOfGovernors.class,
                ValidationConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ServerIpInfoRestControllerTest {
    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/generated-snippets");

    @Autowired
    private ServerIpInfoRepository repository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Value("${spring.application.name}")
    private String applicationName;

    private MockMvc mockMvc;

    private List<ServerIpInfo> serverIpInfos = new ArrayList<>();

    private RestDocumentationResultHandler document;

    @Before
    public void generateBatchOfNetworks() {

        this.document = document("network/{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
        String serverId = "web_server_";
        String name = "web10050";
        String primaryIp = "8.8.8.";
        String secondaryIp = "8.8.9.";
        for (int i = 0; i < 10; i++) {
            ServerIpInfo serverIpInfo = new ServerIpInfo();
            serverIpInfo.setPrimaryIp(primaryIp + i);
            serverIpInfo.setSecondaryIp(secondaryIp + i);
            serverIpInfo.setServerId(serverId + i);
            serverIpInfo.setName(name + i);
            serverIpInfos.add(serverIpInfo);
        }

        repository.save(serverIpInfos);
    }

    @Test
    public void readOne() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/server-ip-info?serverId=web_server_1").accept(MediaType.APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andExpect(jsonPath("primaryIp").value("8.8.8.1"))
                    .andExpect(jsonPath("secondaryIp").value("8.8.9.1"))
                    .andExpect(jsonPath("name").value("web100501"))
                    .andExpect(jsonPath("serverId").value("web_server_1"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void deleteAll() throws Exception {
        repository.deleteAll();
    }
}
