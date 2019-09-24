package ru.majordomo.hms.rc.staff.resources.template;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Document(collection = Template.COLLECTION_NAME)
@TypeAlias(ApplicationServer.TYPE)
@JsonTypeName(ApplicationServer.TYPE)
@Data
@EqualsAndHashCode(callSuper = true)
public class ApplicationServer extends Template {
    @NotNull(message = "language должен быть указан")
    private Language language;

    @NotBlank(message = "version должно быть указано")
    private String version;

    @Override
    public ResourceType getResourceType() {
        return ResourceType.WEBSITE;
    }

    public static final String TYPE = "ApplicationServer";

    public enum Language {
        PHP,
        PERL
    }

    public static class Spec {
        public static final String SECURITY_LEVEL = "security_level";
    }
}
