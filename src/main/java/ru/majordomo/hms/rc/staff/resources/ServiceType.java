package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.majordomo.hms.rc.staff.resources.validation.UniqueNameResource;

import java.io.IOException;

@Document
@UniqueNameResource(ServiceType.class)
@Deprecated
public class ServiceType extends Resource {
    public ServiceType() {
        switchedOn = true;
    }

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
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
}
