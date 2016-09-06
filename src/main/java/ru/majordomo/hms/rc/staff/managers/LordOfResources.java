package ru.majordomo.hms.rc.staff.managers;

import ru.majordomo.hms.rc.staff.Resource;

public abstract class LordOfResources {
    public abstract Resource createResource();
    public abstract void readResource();
}
