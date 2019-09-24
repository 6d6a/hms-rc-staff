package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceSocket;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;
import ru.majordomo.hms.rc.staff.resources.socket.NetworkSocket;

@RestController
@RequestMapping("/service-socket")
public class ServiceSocketRestController extends RestControllerTemplate<ServiceSocket> {
    private MongoOperations mongoOperations;

    @Autowired
    public void setMongoOperations(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Autowired
    public void setGovernor(GovernorOfServiceSocket governor) {
        this.governor = governor;
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_VIEW')")
    @RequestMapping(value = "/{serviceSocketId}", method = RequestMethod.GET)
    public ServiceSocket readOne(@PathVariable String serviceSocketId) {
        return processReadOneQuery(serviceSocketId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_VIEW')")
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<ServiceSocket> readAll(@RequestParam(required=false, defaultValue="") String name) {
        Map<String, String> keyValue = new HashMap<>();
        if (!name.isEmpty()) {
            keyValue.put("name", name);
        }
        if (!keyValue.isEmpty()) {
            return processReadAllWithParamsQuery(keyValue);
        } else {
            return processReadAllQuery();
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_VIEW')")
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET, headers = "X-HMS-Pageable=true")
    public Page<ServiceSocket> readAll(
            @RequestParam(required=false, defaultValue="") String name,
            Pageable pageable
    ) {
        Map<String, String> keyValue = new HashMap<>();
        if (!name.isEmpty()) {
            keyValue.put("name", name);
        }
        if (!keyValue.isEmpty()) {
            return processReadAllWithParamsQuery(keyValue, pageable);
        } else {
            return processReadAllQuery(pageable);
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_VIEW')")
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET, headers = "X-HMS-Projection=OnlyIdAndName")
    public Collection<ServiceSocket> readAll() {
        return processReadAllQueryOnlyIdAndName();
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_CREATE')")
    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<ServiceSocket> create(@RequestBody ServiceSocket socket) throws ParameterValidateException {
        return processCreateQuery(socket);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_EDIT')")
    @RequestMapping(value = "/{serviceSocketId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ServiceSocket> update(@PathVariable String serviceSocketId, @RequestBody ServiceSocket socket) throws ParameterValidateException {
        return processUpdateQuery(serviceSocketId, socket);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_DELETE')")
    @RequestMapping(value = "/{serviceSocketId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable String serviceSocketId) {
        return processDeleteQuery(serviceSocketId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/migrate")
    public Collection<ServiceSocket> migrate() {
        List<ServiceSocket> serviceSockets = governor.buildAll();

        serviceSockets.forEach(serviceSocket -> {
            NetworkSocket socket = new NetworkSocket();
            socket.setId(serviceSocket.getId());
            socket.setAddress(serviceSocket.getAddress());
            socket.setPort(serviceSocket.getPort());
            socket.setName(serviceSocket.getName());
            socket.setSwitchedOn(serviceSocket.getSwitchedOn());

            if (serviceSocket.getName() == null) {
                serviceSocket.setName("");
            }
            String protocol = null;
            Pattern pattern = Pattern.compile(".*-([a-z]+)@.+");
            Matcher matcher = pattern.matcher(serviceSocket.getName());
            while (matcher.find()) {
                protocol = matcher.group(1);
            }

            if (protocol != null) {
                socket.setProtocol(protocol);
            }

            mongoOperations.save(socket);
        });

        return serviceSockets;
    }
}
