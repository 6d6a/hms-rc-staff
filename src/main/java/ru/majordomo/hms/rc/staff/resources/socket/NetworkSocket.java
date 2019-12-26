package ru.majordomo.hms.rc.staff.resources.socket;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.majordomo.hms.rc.staff.resources.Network;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Document(collection = Socket.COLLECTION_NAME)
@TypeAlias(NetworkSocket.TYPE)
@JsonTypeName(NetworkSocket.TYPE)
@Data
@EqualsAndHashCode(callSuper = true)
public class NetworkSocket extends Socket {
    private String protocol;
    @NotNull
    @Range(max = 4294967295L, message = "параметр address указан неверно (должно быть между {min} и {max} в формате Long)")
    private Long address;

    @NotNull
    @Range(min = 1L, max = 65535L, message = "Значение параметра port может находиться в пределах диапазоне 1-65535")
    private Integer port;

    @JsonIgnore
    public Long getAddress() {
        return address;
    }

    @JsonGetter(value = "address")
    public String getAddressAsString() {
        try {
            return Network.ipAddressInIntegerToString(address);
        } catch (NullPointerException e) {
            return null;
        }
    }

    @JsonIgnore
    public void setAddress(Long address) {
        this.address = address;
    }

    @JsonSetter(value = "address")
    public void setAddress(String address) {
        try {
            this.address = Network.ipAddressInStringToInteger(address);
        } catch (NullPointerException ignored) {
        }
    }

    public static final String TYPE = "NetworkSocket";
}
