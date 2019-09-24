package ru.majordomo.hms.rc.staff.resources;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Document
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfigTemplate extends Resource {
    @NotBlank(message = "Адрес не может быть пустым")
    @URL(message = "Параметр fileLink содержит некорретный URL:'${validatedValue}'")
    private String fileLink;

    private String pathTemplate;

    @NotNull(message = "context обязательное поле")
    private ContextType context = ContextType.SERVICE;

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    public enum ContextType {
        SERVICE,
        WEBSITE
    }
}
