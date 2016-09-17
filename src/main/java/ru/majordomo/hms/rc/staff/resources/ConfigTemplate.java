package ru.majordomo.hms.rc.staff.resources;

import org.springframework.data.mongodb.core.mapping.Document;

import java.io.File;

import ru.majordomo.hms.rc.staff.Resource;

@Document
public class ConfigTemplate extends Resource {

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
}
