package ru.majordomo.hms.rc.staff.test.managers;

import org.junit.Assert;
import org.junit.Test;
import ru.majordomo.hms.rc.staff.managers.GovernorOfSocket;

import java.util.UUID;

public class GovernorOfSocketTest {
    @Test
    public void generateFileNameText() {
        Assert.assertEquals("/home/u999/redis.socket", GovernorOfSocket.generateFileWithPrefix("$HOME/redis.socket", "/home/u999", 0));
        Assert.assertEquals("/home/u999/redis_1.socket", GovernorOfSocket.generateFileWithPrefix("$HOME/redis.socket", "/home/u999", 1));
        Assert.assertEquals("/home/u999/redis_9.socket", GovernorOfSocket.generateFileWithPrefix("$HOME/redis.socket", "/home/u999", 9));
        Assert.assertEquals("/home/u999/d.d/redis_9", GovernorOfSocket.generateFileWithPrefix("$HOME/d.d/redis", "/home/u999", 9));
        Assert.assertEquals("/home/u999/d.d/redis_10", GovernorOfSocket.generateFileWithPrefix("$HOME/d.d/redis", "/home/u999/", 10));
        Assert.assertEquals("/tmp/5dcea7aa75c2d9104131408f.socket", GovernorOfSocket.generateFileWithId("/tmp/$ID.socket", "", "5dcea7aa75c2d9104131408f"));
        Assert.assertEquals("/home/u999/5dcea7aa75c2d9104131408f.socket", GovernorOfSocket.generateFileWithId("$HOME/$ID.socket", "/home/u999/", "5dcea7aa75c2d9104131408f"));
    }
}
