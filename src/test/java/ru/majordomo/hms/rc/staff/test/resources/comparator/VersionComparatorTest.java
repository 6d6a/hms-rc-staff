package ru.majordomo.hms.rc.staff.test.resources.comparator;

import org.junit.Assert;
import org.junit.Test;
import ru.majordomo.hms.rc.staff.resources.comparator.VersionComparator;

import java.util.Arrays;
import java.util.List;

public class VersionComparatorTest {
    private final VersionComparator comparator = new VersionComparator();

    @Test
    public void mainTest() {
        Assert.assertEquals("1", sort(null, "1").get(0));
        Assert.assertEquals("2", sort("1", "2").get(0));
        Assert.assertEquals("1.2", sort("1.1", "1.2").get(0));
        Assert.assertEquals("1.2.1", sort("1.1.3", "1.2.1").get(0));
        Assert.assertEquals("1.2.1", sort("a", "1.2.1").get(0));
        Assert.assertEquals("1.2.1", sort("1", null, "1.2.1").get(0));
    }

    private List<String> sort(String... values) {
        List<String> list = Arrays.asList(values);
        list.sort(comparator);
        return list;
    }
}
