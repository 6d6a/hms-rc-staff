package ru.majordomo.hms.rc.staff.resources.template;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = Template.COLLECTION_NAME)
@TypeAlias(HttpServer.TYPE)
@JsonTypeName(HttpServer.TYPE)
@Data
@EqualsAndHashCode(callSuper = true)
public class HttpServer extends Template {
//    private final ResourceType resourceType = ResourceType.WEBSITE;

    @Override
    public ResourceType getResourceType() {
        return ResourceType.WEBSITE;
    }

    public static final String TYPE = "HttpServer";
}
