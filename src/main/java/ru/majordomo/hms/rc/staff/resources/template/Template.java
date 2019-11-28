package ru.majordomo.hms.rc.staff.resources.template;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.resources.validation.ObjectIdCollection;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.*;

@Document(collection = Template.COLLECTION_NAME)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ApplicationServer.class, name = ApplicationServer.TYPE),
        @JsonSubTypes.Type(value = CronD.class, name = CronD.TYPE),
        @JsonSubTypes.Type(value = FtpD.class, name = FtpD.TYPE),
        @JsonSubTypes.Type(value = DatabaseServer.class, name = DatabaseServer.TYPE),
        @JsonSubTypes.Type(value = HttpServer.class, name = HttpServer.TYPE),
        @JsonSubTypes.Type(value = Postfix.class, name = Postfix.TYPE),
        @JsonSubTypes.Type(value = SshD.class, name = SshD.TYPE)
})
@Data
@EqualsAndHashCode(callSuper = true)
//TODO https://jira.spring.io/browse/DATAMONGO-2068
// если добавляется новый наследник, то для корректной конвертации типов нужно создать отдельный repository
public abstract class Template extends Resource {
    /**
     * может ли данный сервис быть привязан к аккаунту.
     */
    @Indexed
    @NotNull
    private Boolean availableToAccounts = false;

    @ObjectIdCollection(value = ConfigTemplate.class, message = "ConfigTemplate с указанным id не найден в БД")
    private List<String> configTemplateIds = new ArrayList<>();

    @Transient
    private List<ConfigTemplate> configTemplates = new ArrayList<>();

    @NotBlank(message = "supervisionType должен быть указан")
    @Pattern(
            regexp = SupervisionType.DOCKER + "|" +
                    SupervisionType.SYSVINIT + "|" +
                    SupervisionType.SYSTEMD + "|" +
                    SupervisionType.UPSTART,
            message = "supervisionType может быть одним из (" +
                    SupervisionType.DOCKER + "|" +
                    SupervisionType.SYSVINIT + "|" +
                    SupervisionType.SYSTEMD + "|" +
                    SupervisionType.UPSTART + ")")
    private String supervisionType;

    private String sourceUri;

    private Map<String, String> resourceSpec = new HashMap<>();

    private Map<String, String> instanceSpec = new HashMap<>();

    //TODO удалить после миграции с serviceTemplate
    private Set<String> migratedServiceTemplateIds = new HashSet<>();

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    public abstract ResourceType getResourceType();

    public void addConfigTemplate(ConfigTemplate configTemplate) {
        if (configTemplate != null) {
            String configTemplateId = configTemplate.getId();
            this.configTemplates.add(configTemplate);
            if (!configTemplateIds.contains(configTemplateId)) {
                this.configTemplateIds.add(configTemplate.getId());
            }
        }
    }

    public void addMigratedServiceTemplateId(String serviceTemplateId) {
        if (migratedServiceTemplateIds == null) {
            migratedServiceTemplateIds = new HashSet<>();
        }
        migratedServiceTemplateIds.add(serviceTemplateId);
    }

    public void addInstanceSpec(String specName,String specType) {
        if (instanceSpec == null) {
            instanceSpec = new HashMap<>();
        }
        instanceSpec.put(specName, specType);
    }

    static final String COLLECTION_NAME = "template";

    public static class SupervisionType {
        public static final String DOCKER = "docker";
        public static final String SYSVINIT = "sysvinit";
        public static final String SYSTEMD = "systemd";
        public static final String UPSTART = "upstart";
    }

    public static class InstanceSpecType {
        public static final String STRING = "string";
        public static final String INTEGER = "integer";
    }
}
