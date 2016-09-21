import com.google.common.net.InetAddresses;
import com.google.common.net.MediaType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Request;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import config.FongoConfig;
import config.GovernorOfNetworkConfig;
import ru.majordomo.hms.rc.staff.api.http.NetworkRestController;
import ru.majordomo.hms.rc.staff.managers.GovernorOfNetwork;
import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.resources.Network;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {FongoConfig.class, GovernorOfNetworkConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NetworkRestControllerTest {
    @Autowired
    GovernorOfNetwork governorOfNetwork;
    @Autowired
    NetworkRepository networkRepository;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private List<String> networkIds = new ArrayList<>();

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
            String gwAddress = addressPattern + thirdByte + ".1";
            Network network = new Network();
            network.setName(name + " " + i);
            network.setSwitchedOn(switchedOn);
            network.setAddress(governorOfNetwork.ipAddressInStringToInteger(address));
            network.setGatewayAddress(governorOfNetwork.ipAddressInStringToInteger(gwAddress));
            network.setVlanNumber(vlanNumber);
            network.setMask(mask);
            networkRepository.save(network);
            networkIds.add(network.getId());
        }
    }

    @Test
    public void readOne() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/rc/network/" + networkIds.get(0)).accept(org.springframework.http.MediaType.APPLICATION_JSON);
        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void readAll() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/rc/network").accept(org.springframework.http.MediaType.APPLICATION_JSON);
        try {
            mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8"));
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
