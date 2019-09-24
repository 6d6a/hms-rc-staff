package ru.majordomo.hms.rc.staff.resources.template;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = Template.COLLECTION_NAME)
@TypeAlias(FtpD.TYPE)
@JsonTypeName(FtpD.TYPE)
@Data
@EqualsAndHashCode(callSuper = true)
public class FtpD extends Template {
//    private final ResourceType resourceType = ResourceType.ACCESS;

    @Override
    public ResourceType getResourceType() {
        return ResourceType.ACCESS;
    }

    public static final String TYPE = "FtpD";
}
