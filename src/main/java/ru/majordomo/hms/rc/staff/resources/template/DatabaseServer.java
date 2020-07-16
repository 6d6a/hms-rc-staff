package ru.majordomo.hms.rc.staff.resources.template;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

@Document(collection = Template.COLLECTION_NAME)
@TypeAlias(DatabaseServer.TYPE)
@JsonTypeName(DatabaseServer.TYPE)
@Data
@EqualsAndHashCode(callSuper = true)
public class DatabaseServer extends Template {
//    private final ResourceType resourceType = ResourceType.DATABASE;

    @NotNull(message = "type должен быть указан")
    private Type type;

    @Override
    public ResourceType getResourceType() {
        return ResourceType.DATABASE;
    }

    public static final String TYPE = "DatabaseServer";

    public enum Type {
        MYSQL,
        POSTGRESQL,
        REDIS,
        MEMCACHED
    }
}
