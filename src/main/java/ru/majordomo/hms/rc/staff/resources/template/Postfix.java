package ru.majordomo.hms.rc.staff.resources.template;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = Template.COLLECTION_NAME)
@TypeAlias(Postfix.TYPE)
@JsonTypeName(Postfix.TYPE)
@Data
@EqualsAndHashCode(callSuper = true)
public class Postfix extends Template {
//    private final ResourceType resourceType = ResourceType.STAFF;

    @Override
    public ResourceType getResourceType() {
        return ResourceType.STAFF;
    }

    public static final String TYPE = "Postfix";
}
