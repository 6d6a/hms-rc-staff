package ru.majordomo.hms.rc.staff.test.resources.comparator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.comparator.StaffServiceComparator;
import ru.majordomo.hms.rc.staff.resources.template.ApplicationServer;
import ru.majordomo.hms.rc.staff.resources.template.DatabaseServer;
import ru.majordomo.hms.rc.staff.resources.template.HttpServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StaffServiceComparatorTest {
    private final StaffServiceComparator comparator = new StaffServiceComparator();

    private Service off;
    private Service php74;
    private Service php73;
    private Service php74h;
    private Service personal;
    private Service personalOff;
    private Service perl;
    private Service http;
    private Service db;

    @Before
    public void setUp() {
        ApplicationServer applicationServer;

        off = new Service();
        applicationServer = new ApplicationServer();
        off.setId("off");
        off.setSwitchedOn(false);
        off.setAccountId(null);
        off.addInstanceProp(ApplicationServer.Spec.SECURITY_LEVEL, ApplicationServer.Security.DEFAULT);
        off.setTemplate(applicationServer);
        applicationServer.setVersion("7.5");
        applicationServer.setLanguage(ApplicationServer.Language.PHP);

        php74 = new Service();
        applicationServer = new ApplicationServer();
        php74.setId("7.4");
        php74.setSwitchedOn(true);
        php74.setAccountId(null);
        php74.addInstanceProp(ApplicationServer.Spec.SECURITY_LEVEL, ApplicationServer.Security.DEFAULT);
        php74.setTemplate(applicationServer);
        applicationServer.setVersion("7.4");
        applicationServer.setLanguage(ApplicationServer.Language.PHP);

        php74h = new Service();
        applicationServer = new ApplicationServer();
        php74h.setId("7.4h");
        php74h.setSwitchedOn(true);
        php74h.setAccountId(null);
        php74h.addInstanceProp(ApplicationServer.Spec.SECURITY_LEVEL, ApplicationServer.Security.HARDENED);
        php74h.setTemplate(applicationServer);
        applicationServer.setVersion("7.4");
        applicationServer.setLanguage(ApplicationServer.Language.PHP);

        personal = new Service();
        applicationServer = new ApplicationServer();
        personal.setId("personal");
        personal.setSwitchedOn(true);
        personal.setAccountId("1");
        personal.addInstanceProp(ApplicationServer.Spec.SECURITY_LEVEL, ApplicationServer.Security.DEFAULT);
        personal.setTemplate(applicationServer);
        applicationServer.setVersion("7.3");
        applicationServer.setLanguage(ApplicationServer.Language.PHP);

        php73 = new Service();
        applicationServer = new ApplicationServer();
        php73.setId("7.3");
        php73.setSwitchedOn(true);
        php73.setAccountId("");
        php73.setTemplate(applicationServer);
        applicationServer.setVersion("7.3");
        applicationServer.setLanguage(ApplicationServer.Language.PHP);

        personalOff = new Service();
        applicationServer = new ApplicationServer();
        personalOff.setId("personal-off");
        personalOff.setSwitchedOn(false);
        personalOff.setAccountId("1");
        personalOff.addInstanceProp(ApplicationServer.Spec.SECURITY_LEVEL, ApplicationServer.Security.DEFAULT);
        personalOff.setTemplate(applicationServer);
        applicationServer.setVersion("7.5");
        applicationServer.setLanguage(ApplicationServer.Language.PHP);

        perl = new Service();
        applicationServer = new ApplicationServer();
        perl.setId("perl");
        perl.setSwitchedOn(true);
        perl.setAccountId("");
        perl.setTemplate(applicationServer);
        applicationServer.setVersion("7.5");
        applicationServer.setLanguage(ApplicationServer.Language.PERL);

        http = new Service();
        http.setSwitchedOn(true);
        http.setTemplate(new HttpServer());

        db = new Service();
        db.setSwitchedOn(true);
        db.setTemplate(new DatabaseServer());

    }

    @Test
    public void test() {
        List<Service> services = new ArrayList<>();

        services.add(off);
        services.add(php74);
        services.add(php74h);
        services.add(personal);
        services.add(php73);
        services.add(personalOff);
        services.add(perl);
        services.add(http);
        services.add(db);
        services.add(null);

        services.sort(comparator);

        Assert.assertEquals(services.get(0), personal);
        Assert.assertEquals(services.get(1), php74);
        Assert.assertEquals(services.get(2), php74h);
        Assert.assertEquals(services.get(3), php73);
        Assert.assertEquals(services.get(4), perl);
        Assert.assertEquals(services.get(5), http);

    }

    @Test
    public void test2() {
        List<Service> otherServices = Arrays.asList(personal, personalOff, http);
        otherServices.sort(comparator);

        Assert.assertEquals(otherServices.get(0), personal);
        Assert.assertEquals(otherServices.get(1), http);
        Assert.assertEquals(otherServices.get(2), personalOff);

    }
}
