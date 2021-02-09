package ru.majordomo.hms.rc.staff.api.message;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ServiceMessage {
    static final Logger logger = LoggerFactory.getLogger(ServiceMessage.class);

    /** personmgr.ProcessingBusinessOperation.id */
    @Nullable
    private String operationIdentity;

    /** personmgr.ProcessingBusinessAction.id */
    @Nullable
    private String actionIdentity;

    /** ссылка на ресурс, например: http://rc-staff/service/MONGO_ID */
    private String objRef;

    @Nonnull
    private Map<Object,Object> params = new HashMap<>();

    @Nullable
    private String accountId;

    public Object getParam(String param) {
        return params.get(param);
    }

    public void addParam(Object name, Object value) {
        params.put(name,value);
    }

    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = "";
        try {
            jsonData = objectMapper.writeValueAsString(this);
        } catch (IOException ex) {
            logger.error("Невозможно конвертировать в JSON" + ex.toString());
        }
        return jsonData;
    }

    @Override
    public String toString() {
        return "ServiceMessage{" +
                "operationIdentity='" + operationIdentity + '\'' +
                ", actionIdentity='" + actionIdentity + '\'' +
                ", objRef='" + objRef + '\'' +
                ", params=" + params +
                '}';
    }
}
