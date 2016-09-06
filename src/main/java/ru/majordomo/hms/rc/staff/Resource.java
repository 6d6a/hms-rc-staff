package ru.majordomo.hms.rc.staff;

import org.springframework.data.annotation.Id;

public abstract class Resource {
    @Id
    private String id;
    private String name;
    public Boolean switchedOn;

    public abstract void switchResource();

    public Boolean isSwitchedOn() {
        return switchedOn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
