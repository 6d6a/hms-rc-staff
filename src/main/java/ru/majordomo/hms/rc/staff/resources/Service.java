package ru.majordomo.hms.rc.staff.resources;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import ru.majordomo.hms.rc.staff.resources.socket.Socket;
import ru.majordomo.hms.rc.staff.resources.template.Template;
import ru.majordomo.hms.rc.staff.resources.validation.ObjectId;
import ru.majordomo.hms.rc.staff.resources.validation.ObjectIdCollection;

@Document
@Data
@EqualsAndHashCode(callSuper = true)
public class Service extends Resource implements ConnectableToAccount, ConnectableToServer {
    @Indexed
    private String accountId;

    @Indexed
    private String serverId;

    @Deprecated
    @ObjectId(ServiceTemplate.class)
    private String serviceTemplateId;

    @NotNull(message = "Отсутствует Template")
    @ObjectId(Template.class)
    private String templateId;

    @ObjectIdCollection(Socket.class)
    private List<String> socketIds = new ArrayList<>();

    @Deprecated
    @ObjectIdCollection(ServiceSocket.class)
    private List<String> serviceSocketIds = new ArrayList<>();

    private Map<String, String> instanceProps = new HashMap<>();

    @Deprecated
    @Transient
    private ServiceTemplate serviceTemplate;

    @Transient
    private Template template;

    @Transient
    private List<Socket> sockets = new ArrayList<>();

    @Transient
    @Deprecated
    private List<ServiceSocket> serviceSockets = new ArrayList<>();

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    @Deprecated
    public void setServiceTemplate(ServiceTemplate serviceTemplate) {
        this.serviceTemplateId = serviceTemplate != null ? serviceTemplate.getId() : null;
        this.serviceTemplate = serviceTemplate;
    }

    public void setTemplate(Template template) {
        this.templateId = template != null ? template.getId() : null;
        this.template = template;
    }

    public void setSockets(List<Socket> sockets) {
        List<String> ids = new ArrayList<>();
        for (Socket socket: sockets) {
            ids.add(socket.getId());
        }
        this.socketIds = ids;
        this.sockets = sockets;
    }

    @Deprecated
    public void setServiceSockets(List<ServiceSocket> serviceSockets) {
        List<String> ids = new ArrayList<>();
        for (ServiceSocket serviceSocket: serviceSockets) {
            ids.add(serviceSocket.getId());
        }
        this.serviceSocketIds = ids;
        this.serviceSockets = serviceSockets;
    }

    @Deprecated
    public void addServiceSocket(ServiceSocket serviceSocket) {
        String serviceSocketId = serviceSocket.getId();
        this.serviceSockets.add(serviceSocket);
        if (!serviceSocketIds.contains(serviceSocketId)) {
            this.serviceSocketIds.add(serviceSocket.getId());
        }
    }

    public void addServiceSocketId(String serviceSocketId) {
        this.serviceSocketIds.add(serviceSocketId);
    }

    public void addSocket(Socket socket) {
        this.sockets.add(socket);
        if (!socketIds.contains(socket.getId())) {
            this.socketIds.add(socket.getId());
        }
    }

    public void addInstanceProp(String propName,String propValue) {
        if (instanceProps == null) {
            instanceProps = new HashMap<>();
        }
        instanceProps.put(propName, propValue);
    }

    @Override
    public String toString() {
        return "Service{" +
                "serviceTemplate=" + serviceTemplate +
                ", serviceSockets=" + serviceSockets +
                ", serviceTemplateId='" + serviceTemplateId + '\'' +
                ", serviceSocketIds=" + serviceSocketIds +
                "} " + super.toString();
    }
}
