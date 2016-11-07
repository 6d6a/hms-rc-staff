package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServer;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.resources.Server;

@RestController
@RequestMapping(value = "/server")
public class ServerRestController extends RestControllerTemplate {

    @Autowired
    public void setGovernor(GovernorOfServer governor) {
        this.governor = governor;
    }

    @RequestMapping(value = "/{serverId}", method = RequestMethod.GET)
    public Server readOne(@PathVariable String serverId) {
        return (Server) processReadOneQuery(serverId);
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<? extends Resource> readAll(@RequestParam(required=false) Map<String,String> requestParams) {
        if (!requestParams.isEmpty()) {
            return processReadAllWithParamsQuery(requestParams);
        } else {
            return processReadAllQuery();
        }
    }

    @RequestMapping(value = {"/filter"}, method = RequestMethod.GET)
    public Server readOneFilter(@RequestParam() Map<String,String> requestParams) {
        return (Server) processReadOneWithParamsQuery(requestParams);
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<?> create (@RequestBody Server server) throws ParameterValidateException {
        return processCreateQuery(server);
    }

    @RequestMapping(value = "/{serverId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<?> update(@PathVariable String serverId, @RequestBody Server server) throws ParameterValidateException {
        return processUpdateQuery(serverId, server);
    }

    @RequestMapping(value = "/{serverId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable String serverId) {
        return processDeleteQuery(serverId);
    }
}
