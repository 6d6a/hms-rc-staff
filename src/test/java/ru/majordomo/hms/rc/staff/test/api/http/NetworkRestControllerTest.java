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

import ru.majordomo.hms.rc.staff.test.config.*;
import ru.majordomo.hms.rc.staff.managers.GovernorOfNetwork;
import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.resources.Network;

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
public class NetworkRestControllerTest {
    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/generated-snippets");

    @Autowired
    private GovernorOfNetwork governorOfNetwork;
    @Autowired
    private NetworkRepository networkRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Value("${spring.application.name}")
    private String applicationName;

    private MockMvc mockMvc;

    private List<Network> networks = new ArrayList<>();

    private String resourceName = "network";

    private RestDocumentationResultHandler document;

    @Before
    public void generateBatchOfNetworks() {

        this.document = document("network/{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();

        String name = "Тестовая сеть";
        Boolean switchedOn = Boolean.TRUE;
        Integer thirdByte = 10;
        Integer gwHostId = 1;
        String addressPattern = "10.10.";
        Integer vlanNumber = 103;
        Integer mask = 24;

        for (int i = 0; i < 10; i++) {
            thirdByte = thirdByte + 1;
            String address = addressPattern + thirdByte + ".0";
            String gwAddress = addressPattern + thirdByte + "." + gwHostId;
            Network network = new Network();
            network.setName(name + " " + i);
            network.setSwitchedOn(switchedOn);
            network.setAddress(address);
            network.setGatewayAddress(gwAddress);
            network.setVlanNumber(vlanNumber);
            network.setMask(mask);
            networkRepository.save(network);
            networks.add(network);
        }
    }

    @Test
    public void readOne() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/" + networks.get(0).getId()).accept(MediaType.APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(this.document)
                    .andDo(this.document.document(
                    responseFields(
                            fieldWithPath("id").description("Network ID"),
                            fieldWithPath("name").description("Имя Network"),
                            fieldWithPath("switchedOn").description("Статус Network"),
                            fieldWithPath("address").description("IP-адрес"),
                            fieldWithPath("mask").description("Маска"),
                            fieldWithPath("gatewayAddress").description("Шлюз"),
                            fieldWithPath("vlanNumber").description("Номер сети")
                    )
            ));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAll() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName).accept(MediaType.APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(this.document)
                    .andDo(this.document.document(
                    responseFields(
                            fieldWithPath("[].id").description("Network ID"),
                            fieldWithPath("[].name").description("Имя Network"),
                            fieldWithPath("[].switchedOn").description("Статус Network"),
                            fieldWithPath("[].address").description("IP-адрес"),
                            fieldWithPath("[].mask").description("Маска"),
                            fieldWithPath("[].gatewayAddress").description("Шлюз"),
                            fieldWithPath("[].vlanNumber").description("Номер сети")
                    )
            ));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAllByName() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName).param("name", networks.get(2).getName()).accept(MediaType.APPLICATION_JSON_UTF8);

        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                    .andDo(this.document)
                    .andDo(this.document.document(
                            responseFields(
                                    fieldWithPath("[].id").description("Network ID"),
                                    fieldWithPath("[].name").description("Имя Network"),
                                    fieldWithPath("[].switchedOn").description("Статус Network"),
                                    fieldWithPath("[].address").description("IP-адрес"),
                                    fieldWithPath("[].mask").description("Маска"),
                                    fieldWithPath("[].gatewayAddress").description("Шлюз"),
                                    fieldWithPath("[].vlanNumber").description("Номер сети")
                            )
                    ));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readOneAndCheckObjectFields() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + resourceName + "/" + networks.get(0).getId()).accept(MediaType.APPLICATION_JSON_UTF8);

        try {
            Network testingNetwork = networks.get(0);
            mockMvc.perform(request).andExpect(jsonPath("gatewayAddress").value(testingNetwork.getGatewayAddressAsString()))
                    .andExpect(jsonPath("address").value(testingNetwork.getAddressAsString()))
                    .andExpect(jsonPath("switchedOn").value(testingNetwork.getSwitchedOn()))
                    .andExpect(jsonPath("vlanNumber").value(testingNetwork.getVlanNumber()))
                    .andExpect(jsonPath("mask").value(testingNetwork.getMask()))
                    .andExpect(jsonPath("name").value(testingNetwork.getName()))
                    .andExpect(jsonPath("id").value(testingNetwork.getId()))
                    .andDo(this.document)
                    .andDo(this.document.document(
                            responseFields(
                                    fieldWithPath("id").description("Network ID"),
                                    fieldWithPath("name").description("Имя Network"),
                                    fieldWithPath("switchedOn").description("Статус Network"),
                                    fieldWithPath("address").description("IP-адрес"),
                                    fieldWithPath("mask").description("Маска"),
                                    fieldWithPath("gatewayAddress").description("Шлюз"),
                                    fieldWithPath("vlanNumber").description("Номер сети")
                            )
                    ));
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void create() {
        Network testingNetwork = networks.get(0);
        testingNetwork.setId(null);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/" + resourceName)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(testingNetwork.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isCreated()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void update() {
        Network network = networks.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + resourceName + "/" + network.getId())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(network.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isOk()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void updateNotExistingResource() {
        Network network = networks.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/" + resourceName + "/" + ObjectId.get().toString())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(network.toJson());
        try {
            mockMvc.perform(request).andExpect(status().isNotFound()).andDo(this.document);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void delete() {
        Network network = networks.get(0);
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/" + resourceName + "/" + network.getId())
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

    @After
    public void cleanAll() {
        networkRepository.deleteAll();
    }
}
