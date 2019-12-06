package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfSocket;
import ru.majordomo.hms.rc.staff.resources.socket.Socket;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/socket")
public class SocketRestController extends RestControllerTemplate<Socket> {
    @Autowired
    public void setGovernor(GovernorOfSocket governor) {
        this.governor = governor;
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_VIEW')")
    @RequestMapping(value = "/{serviceSocketId}", method = RequestMethod.GET)
    public Socket readOne(@PathVariable String serviceSocketId) {
        return processReadOneQuery(serviceSocketId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_VIEW')")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Collection<Socket> readAll(
            @RequestParam(required=false, defaultValue="") String name,
            @RequestParam(required=false, defaultValue="") String regex
    ) {
        Map<String, String> keyValue = new HashMap<>();
        if (!name.isEmpty()) {
            keyValue.put("name", name);
        }
        if (!regex.isEmpty()) {
            keyValue.put("regex", "true");
        }
        if (!keyValue.isEmpty()) {
            return processReadAllWithParamsQuery(keyValue);
        } else {
            return processReadAllQuery();
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_VIEW')")
    @RequestMapping(value = "", method = RequestMethod.GET, headers = "X-HMS-Pageable=true")
    public Page<Socket> readAll(
            @RequestParam(required=false, defaultValue="") String name,
            @RequestParam(required=false, defaultValue="") String regex,
            Pageable pageable
    ) {
        Map<String, String> keyValue = new HashMap<>();
        if (!name.isEmpty()) {
            keyValue.put("name", name);
        }
        if (!regex.isEmpty()) {
            keyValue.put("regex", "true");
        }
        if (!keyValue.isEmpty()) {
            return processReadAllWithParamsQuery(keyValue, pageable);
        } else {
            return processReadAllQuery(pageable);
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_VIEW')")
    @RequestMapping(value = "", method = RequestMethod.GET, headers = "X-HMS-Projection=OnlyIdAndName")
    public Collection<Socket> readAll() {
        return processReadAllQueryOnlyIdAndName();
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_CREATE')")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Socket> create(@RequestBody Socket socket) throws ParameterValidateException {
        return processCreateQuery(socket);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_EDIT')")
    @RequestMapping(value = "/{serviceSocketId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<Socket> update(@PathVariable String serviceSocketId, @RequestBody Socket socket) throws ParameterValidateException {
        return processUpdateQuery(serviceSocketId, socket);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVICE_SOCKET_DELETE')")
    @RequestMapping(value = "/{serviceSocketId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable String serviceSocketId) {
        return processDeleteQuery(serviceSocketId);
    }
}
