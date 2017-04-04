package ru.majordomo.hms.rc.staff.api.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServer;
import ru.majordomo.hms.rc.staff.resources.Server;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.Storage;

@RestController
@RequestMapping(value = "/server")
public class ServerRestController extends RestControllerTemplate<Server> {
    private static final Logger logger = LoggerFactory.getLogger(ServerRestController.class);

    @Autowired
    public void setGovernor(GovernorOfServer governor) {
        this.governor = governor;
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_VIEW')")
    @RequestMapping(value = "/{serverId}", method = RequestMethod.GET)
    public Server readOne(@PathVariable String serverId) {
        return processReadOneQuery(serverId);
    }

    //Возвращает список объектов Service
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @RequestMapping(value = "/{serverId}/services", method = RequestMethod.GET)
    public Collection<Service> readAllServices(@PathVariable String serverId, @RequestParam Map<String,String> requestParams) {
        requestParams.put("serverId", serverId);
        Collection<Server> servers = processReadAllWithParamsQuery(requestParams);
        Collection<Service> services = new ArrayList<>();
        for (Server server : servers) {
            services.addAll(server.getServices());
        }
        return services;
    }

    //Возвращает ActiveMailboxStorage
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @RequestMapping(value = "/{serverId}/active-storage", method = RequestMethod.GET)
    public Storage readActiveMailboxStorage(@PathVariable String serverId) {
        Map<String,String> requestParams = new HashMap<>();
        requestParams.put("server-id", serverId);
        requestParams.put("active-storage", "true");
        Server server = processReadOneWithParamsQuery(requestParams);
        return server.getActiveMailboxStorage();
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_VIEW')")
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<Server> readAll(@RequestParam(required=false) Map<String,String> requestParams) {
        if (!requestParams.isEmpty()) {
            return processReadAllWithParamsQuery(requestParams);
        } else {
            return processReadAllQuery();
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_VIEW')")
    @RequestMapping(value = {"/filter"}, method = RequestMethod.GET)
    public Server readOneFilter(@RequestParam() Map<String,String> requestParams) {
        return processReadOneWithParamsQuery(requestParams);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_CREATE')")
    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<Server> create (@RequestBody Server server) throws ParameterValidateException {
        return processCreateQuery(server);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_EDIT')")
    @RequestMapping(value = "/{serverId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<Server> update(@PathVariable String serverId, @RequestBody Server server) throws ParameterValidateException {
        return processUpdateQuery(serverId, server);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_DELETE')")
    @RequestMapping(value = "/{serverId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable String serverId) {
        return processDeleteQuery(serverId);
    }
}
