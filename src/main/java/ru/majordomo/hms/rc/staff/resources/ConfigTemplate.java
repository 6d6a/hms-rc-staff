package ru.majordomo.hms.rc.staff.resources;

import org.springframework.data.mongodb.core.mapping.Document;

import java.io.File;

import ru.majordomo.hms.rc.staff.Resource;

@Document
public class ConfigTemplate extends Resource {

    private String fileName;
    private String fileLink;

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    public String getContent() {
        return fileLink;
    }

    public void setContent(String fileLink) {
        this.fileLink = fileLink;
    }
}
