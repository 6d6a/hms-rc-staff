package ru.majordomo.hms.rc.staff.resources.socket;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;

@Document(collection = Socket.COLLECTION_NAME)
@TypeAlias(UnixSocket.TYPE)
@JsonTypeName(UnixSocket.TYPE)
@Data
@EqualsAndHashCode(callSuper = true)
public class UnixSocket extends Socket {
    @NotBlank(message = "file должен быть указан")
    private String file;

    public static final String TYPE = "UnixSocket";
}
