package ru.majordomo.hms.rc.staff.test.resources;

import org.bson.types.ObjectId;
import org.junit.Test;

import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;

import static junit.framework.TestCase.assertTrue;

public class ConfigTemplateTest {
    @Test
    public void equals() throws Exception {
        ConfigTemplate standard = new ConfigTemplate();
        ConfigTemplate tested = new ConfigTemplate();

        standard.setId(ObjectId.get().toString());
        standard.setName("name");
        standard.setSwitchedOn(true);
        standard.setFileLink("http://storage/" + ObjectId.get().toString());

        tested.setId(standard.getId());
        tested.setName(standard.getName());
        tested.setSwitchedOn(standard.getSwitchedOn());
        tested.setFileLink(standard.getFileLink());

        assertTrue(standard.equals(tested));
    }
}
