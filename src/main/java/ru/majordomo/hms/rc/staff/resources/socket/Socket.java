package ru.majordomo.hms.rc.staff.resources.socket;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.majordomo.hms.rc.staff.resources.Resource;

@Document(collection = Socket.COLLECTION_NAME)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = NetworkSocket.class, name = NetworkSocket.TYPE),
        @JsonSubTypes.Type(value = UnixSocket.class, name = UnixSocket.TYPE)
})
@EqualsAndHashCode(callSuper = true)
//TODO https://jira.spring.io/browse/DATAMONGO-2068
// если добавляется новый наследник, то для корректной конвертации типов нужно создать отдельный repository
public abstract class Socket extends Resource {
    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    static final String COLLECTION_NAME = "socket";
}
