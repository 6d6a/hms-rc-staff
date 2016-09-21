import com.google.common.net.InetAddresses;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;

import config.FongoConfig;
import config.GovernorOfNetworkConfig;
import config.GovernorOfServiceTemplateConfig;
import ru.majordomo.hms.rc.staff.api.http.NetworkRestController;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfNetwork;
import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.resources.Network;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {FongoConfig.class, GovernorOfNetworkConfig.class})
public class GovernorOfNetworkTest {

    @Autowired
    GovernorOfNetwork governorOfNetwork;

    @Autowired
    NetworkRepository networkRepository;

    private String name = "Тестовая сеть";
    private Boolean switchedOn = Boolean.TRUE;
    private String address = "172.16.103.0";
    private Integer mask = 24;
    private String gatewayAddress = "172.16.103.1";
    private Integer vlanNumber = 103;
    ServiceMessage serviceMessage;

    private ServiceMessage createServiceMessage(String name, Boolean switchedOn, String address, Integer mask, String gatewayAddress, Integer vlanNumber) {
        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.setActionIdentity(ObjectId.get().toString());
        serviceMessage.setOperationIdentity(ObjectId.get().toString());
        serviceMessage.setObjRef(null);
        serviceMessage.addParam("name", name);
        serviceMessage.addParam("switchedOn", switchedOn);
        serviceMessage.addParam("address", address);
        serviceMessage.addParam("mask", mask);
        serviceMessage.addParam("gatewayAddress", gatewayAddress);
        serviceMessage.addParam("vlanNumber", vlanNumber);

        return serviceMessage;
    }

    @Test
    public void addressConversions() {
        Integer ipInteger = governorOfNetwork.ipAddressInStringToInteger(address);
        Assert.assertEquals("Коневертация адреса из строки в integer и/или обратно проходит некорректно",
                address,
                governorOfNetwork.ipAddressInIntegerToString(ipInteger));
    }

    @Test
    public void create() {
        try {
            serviceMessage = createServiceMessage(name,switchedOn,address,mask,gatewayAddress,vlanNumber);
            Network network = (Network) governorOfNetwork.createResource(serviceMessage);
            Network network1 = networkRepository.findOne(network.getId());
            Assert.assertEquals("Имя сети, полученное из базы не совпадает с ожидаемым", name, network1.getName());
            Assert.assertEquals("Флаг switchedOn не совпадает с ожидаемым", switchedOn, network1.getSwitchedOn());
            Assert.assertEquals("Адрес сети не совпадает с ожидаемым", governorOfNetwork.ipAddressInStringToInteger(address), network1.getAddress());
            Assert.assertEquals("Маска сети не совпадает с ожидаемой", mask, network1.getMask());
            Assert.assertEquals("Gateway не совпадает с ожидаемым", governorOfNetwork.ipAddressInStringToInteger(gatewayAddress), network1.getGatewayAddress());
            Assert.assertEquals("Номер VLAN'а не совпадает с ожидаемым", vlanNumber, network1.getVlanNumber());
        } catch (ParameterValidateException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(expected = ParameterValidateException.class)
    public void createWithBadAddress() throws ParameterValidateException {
        serviceMessage = createServiceMessage(name,switchedOn,"<? yabidabido?>", mask, gatewayAddress, vlanNumber);
        governorOfNetwork.createResource(serviceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void createWithBadGatewayAddress() throws ParameterValidateException {
        serviceMessage = createServiceMessage(name,switchedOn,address,mask,"<? SELECT *",vlanNumber);
        governorOfNetwork.createResource(serviceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void gatewayNotInNetwork() throws ParameterValidateException {
        serviceMessage = createServiceMessage(name,switchedOn,address,mask,"10.10.10.1",vlanNumber);
        governorOfNetwork.createResource(serviceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void maskOutOfRange() throws ParameterValidateException {
        serviceMessage = createServiceMessage(name,switchedOn,address,101,gatewayAddress,vlanNumber);
        governorOfNetwork.createResource(serviceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void vlanOutOfRange() throws ParameterValidateException {
        serviceMessage = createServiceMessage(name,switchedOn,address,mask,gatewayAddress,-1);
        governorOfNetwork.createResource(serviceMessage);
    }
}
