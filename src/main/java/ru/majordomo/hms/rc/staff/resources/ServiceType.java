package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.IOException;

@Document
public class ServiceType {
    @Indexed(unique = true)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
