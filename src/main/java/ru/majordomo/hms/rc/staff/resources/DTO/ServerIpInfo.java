package ru.majordomo.hms.rc.staff.resources.DTO;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ServerIpInfo {
    @Id
    @Indexed
    private String id;
    private String name;
    private String serverId;
    private String primaryIp;
    private String secondaryIp;

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

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getPrimaryIp() {
        return primaryIp;
    }

    public void setPrimaryIp(String primaryIp) {
        this.primaryIp = primaryIp;
    }

    public String getSecondaryIp() {
        return secondaryIp;
    }

    public void setSecondaryIp(String secondaryIp) {
        this.secondaryIp = secondaryIp;
    }
}
