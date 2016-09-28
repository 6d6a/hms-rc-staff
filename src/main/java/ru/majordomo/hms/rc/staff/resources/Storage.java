package ru.majordomo.hms.rc.staff.resources;

import org.springframework.data.mongodb.core.mapping.Document;

import ru.majordomo.hms.rc.staff.Resource;

@Document
public class Storage extends Resource {

    private Double capacity;
    private Double capacityUsed;

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    public Double getCapacity() {
        return capacity;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    public Double getCapacityUsed() {
        return capacityUsed;
    }

    public void setCapacityUsed(Double capacityUsed) {
        this.capacityUsed = capacityUsed;
    }
}
