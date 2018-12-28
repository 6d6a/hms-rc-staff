package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import java.io.IOException;

import ru.majordomo.hms.rc.staff.common.Views;
import ru.majordomo.hms.rc.staff.resources.validation.group.ServiceTypeChecks;

public abstract class Resource {
    @Id
    @Indexed
    @JsonView(Views.Operator.class)
    private String id;

    @JsonView(Views.Operator.class)
    @Indexed
    @NotBlank
    @Pattern(regexp = "(?ui)(DATABASE_[A-Z]+|WEBSITE_[A-Z0-9]+_[A-Z0-9]+_[A-Z0-9]+|MAILBOX_[A-Z]+)", groups = {ServiceTypeChecks.class})
    private String name;

    @Indexed
    @NotNull
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

    public Boolean getSwitchedOn() {
        return switchedOn;
    }

    public void setSwitchedOn(Boolean switchedOn) {
        this.switchedOn = switchedOn;
    }
    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = "";
        try {
            jsonData = objectMapper.writeValueAsString(this);
        } catch (IOException ex) {
//            logger.error("Невозможно конвертировать в JSON" + ex.toString());
        }
        return jsonData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Resource resource = (Resource) o;

        if (getId() != null ? !getId().equals(resource.getId()) : resource.getId() != null) return false;
        if (getName() != null ? !getName().equals(resource.getName()) : resource.getName() != null) return false;
        return getSwitchedOn() != null ? getSwitchedOn().equals(resource.getSwitchedOn()) : resource.getSwitchedOn() == null;

    }

    @Override
    public String toString() {
        return "Resource{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", switchedOn=" + switchedOn +
                '}';
    }
}
