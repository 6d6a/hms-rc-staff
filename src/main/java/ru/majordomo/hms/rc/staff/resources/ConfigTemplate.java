package ru.majordomo.hms.rc.staff.resources;

import org.hibernate.validator.constraints.URL;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;

@Document
public class ConfigTemplate extends Resource {

    @NotBlank(message = "Адрес не может быть пустым")
    @URL(message = "Параметр fileLink содержит некорретный URL:'${validatedValue}'")
    private String fileLink;

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    public String getFileLink() {
        return fileLink;
    }

    public void setFileLink(String fileLink) {
        this.fileLink = fileLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ConfigTemplate that = (ConfigTemplate) o;

        return getFileLink() != null ? getFileLink().equals(that.getFileLink()) : that.getFileLink() == null;
    }
}
