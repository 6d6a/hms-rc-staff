package ru.majordomo.hms.rc.staff.resources;

import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import ru.majordomo.hms.rc.staff.resources.validation.ValidStorage;

@Document
@ValidStorage
public class Storage extends Resource {
    @NotNull
    @Min(value = 1L, message = "capacity не может быть меньше или равен нулю")
    private Double capacity;

    @NotNull
    @Min(value = 0L, message = "capacityUsed не может быть меньше нуля")
    private Double capacityUsed;

    @NotNull(message = "Нужен mountPoint")
    @Pattern(regexp = "^/.*", message = "Путь должен быть абсолютным")
    private String mountPoint;

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

    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
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

    @Override
    public String toString() {
        return "Storage{" +
                "capacity=" + capacity +
                ", capacityUsed=" + capacityUsed +
                ", mountPoint='" + mountPoint + '\'' +
                "} " + super.toString();
    }
}
