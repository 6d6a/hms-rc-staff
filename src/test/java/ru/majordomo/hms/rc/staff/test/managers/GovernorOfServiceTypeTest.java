package ru.majordomo.hms.rc.staff.test.managers;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceType;
import ru.majordomo.hms.rc.staff.repositories.ServiceTypeRepository;
import ru.majordomo.hms.rc.staff.resources.ServiceType;
import ru.majordomo.hms.rc.staff.test.config.RepositoriesConfig;
import ru.majordomo.hms.rc.staff.test.config.ServiceTypeConfig;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ServiceTypeConfig.class, RepositoriesConfig.class})
public class GovernorOfServiceTypeTest {
    @Autowired
    private GovernorOfServiceType governor;

    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    private ServiceType testServiceType;

    @Before
    public void setUp() {
        String name0 = "database_mysql";
        ServiceType serviceType = new ServiceType();
        serviceType.setName(name0);
        testServiceType = serviceType;
    }

    @Test
    public void build() {
        serviceTypeRepository.save(testServiceType);
        ServiceType buildedServiceType = governor.build(testServiceType.getName());
        try {
            Assert.assertEquals("Имя не совпадает с ожидаемым", testServiceType.getName(), buildedServiceType.getName());
        } catch (ParameterValidateException | NullPointerException e ) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void buildAll() {
        serviceTypeRepository.save(testServiceType);
        List<ServiceType> buildedServiceType = governor.buildAll();
        try {
            Assert.assertEquals("Имя не совпадает с ожидаемым", testServiceType.getName(), buildedServiceType.get(buildedServiceType.size()-1).getName());
        } catch (ParameterValidateException | NullPointerException e ) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test(expected = ParameterValidateException.class)
    public void validateWithInvalidName() throws ParameterValidateException {
        serviceTypeRepository.save(testServiceType);
        ServiceType serviceType = new ServiceType();
        serviceType.setName("database_mysql");
        governor.isValid(serviceType);
    }

    @After
    public void deleteAll() {
        serviceTypeRepository.deleteAll();
    }
}
