package ru.majordomo.hms.rc.staff.test.managers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.ServiceSocketServicesConfig;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceSocket;
import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceSocketRepository;
import ru.majordomo.hms.rc.staff.resources.Network;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ServiceSocketServicesConfig.class, RepositoriesConfig.class})
public class GovernorOfServiceSocketTest {
    @Autowired
    GovernorOfServiceSocket governor;
    @Autowired
    ServiceSocketRepository repository;
    @Autowired
    NetworkRepository networkRepository;

    ServiceSocket testServiceSocket;
    ServiceMessage testServiceMessage;

    private ServiceMessage generateServiceMessage(String name, Boolean switchedOn,
                                                  String address, Integer port) {
        ServiceMessage serviceMessage = new ServiceMessage();
        serviceMessage.addParam("name", name);
        serviceMessage.addParam("switchedOn", switchedOn);
        serviceMessage.addParam("address", address);
        serviceMessage.addParam("port", port);
        return serviceMessage;
    }

    private ServiceSocket generateServiceSocket(String name, Boolean switchedOn,
                                                String address, Integer port) {
        ServiceSocket serviceSocket = new ServiceSocket();
        serviceSocket.setName(name);
        serviceSocket.setSwitchedOn(switchedOn);
        serviceSocket.setAddress(address);
        serviceSocket.setPort(port);
        return serviceSocket;
    }

    private Network generateNetwork(String address, Integer mask, String gw) {
        Network network = new Network();
        network.setAddress(address);
        network.setMask(mask);
        network.setGatewayAddress(gw);
        networkRepository.save(network);
        return network;
    }

    @Before
    public void setUp() {
        String name = "Тестовый сокет";
        Boolean switchedOn = Boolean.TRUE;
        String address = "10.10.10.2";
        Integer port = 22;
        generateNetwork("10.10.10.0", 24, "10.10.10.1");
        testServiceMessage = generateServiceMessage(name, switchedOn, address, port);
        testServiceSocket = generateServiceSocket(name, switchedOn, address, port);
    }

    @Test
    public void create() {
        try {
            ServiceSocket createdServiceSocket = (ServiceSocket) governor.createResource(testServiceMessage);
            Assert.assertEquals("Имя не совпадает с ожидаемым", testServiceSocket.getName(), createdServiceSocket.getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testServiceSocket.getName(), createdServiceSocket.getName());
            Assert.assertEquals("Адрес не совпадает с ожидаемым", testServiceSocket.getAddress(), createdServiceSocket.getAddress());
            Assert.assertEquals("Порт не совпадает с ожидаемым", testServiceSocket.getPort(), createdServiceSocket.getPort());
        } catch (ParameterValidateException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void build() {
        repository.save(testServiceSocket);
        ServiceSocket buildedServiceSocket = (ServiceSocket) governor.build(testServiceSocket.getId());
        try {
            Assert.assertEquals("Имя не совпадает с ожидаемым", testServiceSocket.getName(), buildedServiceSocket.getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testServiceSocket.getName(), buildedServiceSocket.getName());
            Assert.assertEquals("Адрес не совпадает с ожидаемым", testServiceSocket.getAddress(), buildedServiceSocket.getAddress());
            Assert.assertEquals("Порт не совпадает с ожидаемым", testServiceSocket.getPort(), buildedServiceSocket.getPort());
        } catch (ParameterValidateException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void buildAll() {
        repository.save(testServiceSocket);
        List<ServiceSocket> buildedServiceSockets = governor.buildAll();
        try {
            Assert.assertEquals("Имя не совпадает с ожидаемым", testServiceSocket.getName(), buildedServiceSockets.get(buildedServiceSockets.size()-1).getName());
            Assert.assertEquals("Статус включен/выключен не совпадает с ожидаемым", testServiceSocket.getName(), buildedServiceSockets.get(buildedServiceSockets.size()-1).getName());
            Assert.assertEquals("Адрес не совпадает с ожидаемым", testServiceSocket.getAddress(), buildedServiceSockets.get(buildedServiceSockets.size()-1).getAddress());
            Assert.assertEquals("Порт не совпадает с ожидаемым", testServiceSocket.getPort(), buildedServiceSockets.get(buildedServiceSockets.size()-1).getPort());
        } catch (ParameterValidateException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(expected = ParameterValidateException.class)
    public void createWithBadAddressHigher() throws ParameterValidateException {
        ServiceMessage badAddressServiceMessage = testServiceMessage;
        badAddressServiceMessage.addParam("address", "11.20.30.40");
        governor.createResource(badAddressServiceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void createWithBadAddressLower() throws ParameterValidateException {
        ServiceMessage badAddressServiceMessage = testServiceMessage;
        badAddressServiceMessage.addParam("address", "9.20.30.40");
        governor.createResource(badAddressServiceMessage);
    }

    @Test(expected = ParameterValidateException.class)
    public void createWithBadPort() throws ParameterValidateException {
        ServiceMessage badPortServiceMessage = testServiceMessage;
        badPortServiceMessage.addParam("port", -1);
        governor.createResource(badPortServiceMessage);
    }
}
