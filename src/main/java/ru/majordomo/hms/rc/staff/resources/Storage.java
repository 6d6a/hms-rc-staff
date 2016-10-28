package ru.majordomo.hms.rc.staff.resources;

import org.springframework.data.mongodb.core.mapping.Document;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Storage storage = (Storage) o;

        if (getCapacity() != null ? !getCapacity().equals(storage.getCapacity()) : storage.getCapacity() != null)
            return false;
        return getCapacityUsed() != null ? getCapacityUsed().equals(storage.getCapacityUsed()) : storage.getCapacityUsed() == null;

    }
}
