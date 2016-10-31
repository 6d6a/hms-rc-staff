package ru.majordomo.hms.rc.staff.test.managers;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.NetworkServicesConfig;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfNetwork;
import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.resources.Network;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoriesConfig.class, NetworkServicesConfig.class})
public class GovernorOfNetworkTest {

    @Autowired
    private GovernorOfNetwork governorOfNetwork;

    @Autowired
    private NetworkRepository networkRepository;

    private String name = "Тестовая сеть";
    private Boolean switchedOn = Boolean.TRUE;
    private String address = "172.16.103.0";
    private Integer mask = 24;
    private String gatewayAddress = "172.16.103.1";
    private Integer vlanNumber = 103;
    private ServiceMessage serviceMessage;

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
        Network network = new Network();
        network.setAddress(address);
        Assert.assertEquals("Коневертация адреса из строки в integer и/или обратно проходит некорректно",
                address,
                network.getAddressAsString());
    }

    @Test
    public void create() {
        try {
            serviceMessage = createServiceMessage(name,switchedOn,address,mask,gatewayAddress,vlanNumber);
            Network network = (Network) governorOfNetwork.createResource(serviceMessage);
            Network network1 = networkRepository.findOne(network.getId());
            Assert.assertEquals("Имя сети, полученное из базы не совпадает с ожидаемым", name, network1.getName());
            Assert.assertEquals("Флаг switchedOn не совпадает с ожидаемым", switchedOn, network1.getSwitchedOn());
            Assert.assertEquals("Адрес сети не совпадает с ожидаемым", address, network1.getAddressAsString());
            Assert.assertEquals("Маска сети не совпадает с ожидаемой", mask, network1.getMask());
            Assert.assertEquals("Gateway не совпадает с ожидаемым", gatewayAddress, network1.getGatewayAddressAsString());
            Assert.assertEquals("Номер VLAN'а не совпадает с ожидаемым", vlanNumber, network1.getVlanNumber());
        } catch (ParameterValidateException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void build() {
        Network network = new Network();
        network.setName(name);
        network.setSwitchedOn(switchedOn);
        network.setAddress(address);
        network.setMask(mask);
        network.setGatewayAddress(gatewayAddress);
        network.setVlanNumber(vlanNumber);
        networkRepository.save(network);

        Network buildedNetwork = (Network)governorOfNetwork.build(network.getId());
        try {
            Assert.assertEquals("Имя сети, полученное из базы не совпадает с ожидаемым", name, buildedNetwork.getName());
            Assert.assertEquals("Флаг switchedOn не совпадает с ожидаемым", switchedOn, buildedNetwork.getSwitchedOn());
            Assert.assertEquals("Адрес сети не совпадает с ожидаемым", address, buildedNetwork.getAddressAsString());
            Assert.assertEquals("Маска сети не совпадает с ожидаемой", mask, buildedNetwork.getMask());
            Assert.assertEquals("Gateway не совпадает с ожидаемым", gatewayAddress, buildedNetwork.getGatewayAddressAsString());
            Assert.assertEquals("Номер VLAN'а не совпадает с ожидаемым", vlanNumber, buildedNetwork.getVlanNumber());
        } catch (ParameterValidateException | NullPointerException e ) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void buildAll() {
        Network network = new Network();
        network.setName(name);
        network.setSwitchedOn(switchedOn);
        network.setAddress(address);
        network.setMask(mask);
        network.setGatewayAddress(gatewayAddress);
        network.setVlanNumber(vlanNumber);
        networkRepository.save(network);

        List<Network> buildedNetworks = governorOfNetwork.build(null, null);
        try {
            Assert.assertEquals("Имя сети, полученное из базы не совпадает с ожидаемым", name, buildedNetworks.get(buildedNetworks.size()-1).getName());
            Assert.assertEquals("Флаг switchedOn не совпадает с ожидаемым", switchedOn, buildedNetworks.get(buildedNetworks.size()-1).getSwitchedOn());
            Assert.assertEquals("Адрес сети не совпадает с ожидаемым", address, buildedNetworks.get(buildedNetworks.size()-1).getAddressAsString());
            Assert.assertEquals("Маска сети не совпадает с ожидаемой", mask, buildedNetworks.get(buildedNetworks.size()-1).getMask());
            Assert.assertEquals("Gateway не совпадает с ожидаемым", gatewayAddress, buildedNetworks.get(buildedNetworks.size()-1).getGatewayAddressAsString());
            Assert.assertEquals("Номер VLAN'а не совпадает с ожидаемым", vlanNumber, buildedNetworks.get(buildedNetworks.size()-1).getVlanNumber());
        } catch (ParameterValidateException | NullPointerException e ) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(expected = ParameterValidateException.class)
    public void createResourceWithInvalidAddress() throws ParameterValidateException {
        serviceMessage = createServiceMessage(name, switchedOn, "<? yabidabido?>", mask, gatewayAddress, vlanNumber);
        governorOfNetwork.createResource(serviceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void createResourceWithInvalidGatewayAddress() throws ParameterValidateException {
        serviceMessage = createServiceMessage(name, switchedOn, address, mask, "<? SELECT *", vlanNumber);
        governorOfNetwork.createResource(serviceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void createResourceWithGatewayNotInNetwork() throws ParameterValidateException {
        serviceMessage = createServiceMessage(name, switchedOn, address, mask, "10.10.10.1", vlanNumber);
        governorOfNetwork.createResource(serviceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void createResourceWithMaskOutOfRange() throws ParameterValidateException {
        serviceMessage = createServiceMessage(name, switchedOn, address, 101, gatewayAddress, vlanNumber);
        governorOfNetwork.createResource(serviceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void createResourceWithVlanOutOfRange() throws ParameterValidateException {
        serviceMessage = createServiceMessage(name, switchedOn, address, mask, gatewayAddress, -1);
        governorOfNetwork.createResource(serviceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void validateWithInvalidAddress() throws ParameterValidateException {
        serviceMessage = createServiceMessage(name, switchedOn, address, mask, gatewayAddress, vlanNumber);
        Network network = (Network) governorOfNetwork.createResource(serviceMessage);
        network.setAddress(-1L);
        governorOfNetwork.isValid(network);
    }

    @Test(expected = ParameterValidateException.class)
    public void validateWithInvalidGatewayNotInNetwork() throws ParameterValidateException {
        serviceMessage = createServiceMessage(name, switchedOn, address, mask, gatewayAddress, 103);
        Network network = (Network) governorOfNetwork.createResource(serviceMessage);
        network.setGatewayAddress("10.10.10.1");
        governorOfNetwork.isValid(network);
    }

    @Test(expected = ParameterValidateException.class)
    public void validateWithMaskOutOfRange() throws ParameterValidateException {
        serviceMessage = createServiceMessage(name, switchedOn, address, mask, gatewayAddress, vlanNumber);
        Network network = (Network) governorOfNetwork.createResource(serviceMessage);
        network.setMask(101);
        governorOfNetwork.isValid(network);
    }

    @Test(expected = ParameterValidateException.class)
    public void validateWithVlanOutOfRange() throws ParameterValidateException {
        serviceMessage = createServiceMessage(name, switchedOn, address, mask, gatewayAddress, vlanNumber);
        Network network = (Network) governorOfNetwork.createResource(serviceMessage);
        network.setVlanNumber(-1);
        governorOfNetwork.isValid(network);
    }
}