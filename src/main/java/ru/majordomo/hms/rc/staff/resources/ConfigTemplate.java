package ru.majordomo.hms.rc.staff.resources;

import org.springframework.data.mongodb.core.mapping.Document;

import java.io.File;

import ru.majordomo.hms.rc.staff.Resource;

@Document
public class ConfigTemplate extends Resource {

    private File content;

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    public File getContent() {
        return content;
    }

    public void setContent(File content) {
        this.content = content;
    }
}
