package ru.majordomo.hms.rc.staff.resources.template;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = Template.COLLECTION_NAME)
@TypeAlias(CronD.TYPE)
@JsonTypeName(CronD.TYPE)
@Data
@EqualsAndHashCode(callSuper = true)
public class CronD extends Template {
    @Override
    public ResourceType getResourceType() {
        return ResourceType.UNIX_ACCOUNT;
    }

    public static final String TYPE = "CronD";
}
