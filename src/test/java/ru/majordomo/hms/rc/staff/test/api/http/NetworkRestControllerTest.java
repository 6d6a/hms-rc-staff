package ru.majordomo.hms.rc.staff.test.api.http;

import org.junit.After;
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

import ru.majordomo.hms.rc.staff.test.config.EmbeddedServltetContainerConfig;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.NetworkServicesConfig;
import ru.majordomo.hms.rc.staff.managers.GovernorOfNetwork;
import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.resources.Network;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoriesConfig.class, NetworkServicesConfig.class, EmbeddedServltetContainerConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NetworkRestControllerTest {
    @Autowired
    GovernorOfNetwork governorOfNetwork;
    @Autowired
    NetworkRepository networkRepository;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Value("${spring.application.name}")
    private String applicationName;

    private MockMvc mockMvc;

    private List<Network> networks = new ArrayList<>();

    @Before
    public void generateBatchOfNetworks() {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

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
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName + "/network/" + networks.get(0).getId()).accept(MediaType.APPLICATION_JSON);
        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAll() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName + "/network").accept(MediaType.APPLICATION_JSON);
        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readOneAndCheckObjectFields() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/" + applicationName + "/network/" + networks.get(0).getId()).accept(MediaType.APPLICATION_JSON);
        try {
            Network testingNetwork = networks.get(0);
            mockMvc.perform(request).andExpect(jsonPath("gatewayAddress").value(testingNetwork.getGatewayAddressAsString()))
                    .andExpect(jsonPath("address").value(testingNetwork.getAddressAsString()))
                    .andExpect(jsonPath("switchedOn").value(testingNetwork.getSwitchedOn()))
                    .andExpect(jsonPath("vlanNumber").value(testingNetwork.getVlanNumber()))
                    .andExpect(jsonPath("mask").value(testingNetwork.getMask()))
                    .andExpect(jsonPath("name").value(testingNetwork.getName()))
                    .andExpect(jsonPath("id").value(testingNetwork.getId()));
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void cleanAll() {
        networkRepository.deleteAll();
    }
}
