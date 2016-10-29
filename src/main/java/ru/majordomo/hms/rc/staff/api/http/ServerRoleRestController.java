package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.Collection;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServerRole;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.resources.ServerRole;

@RestController
@RequestMapping(value = "/server-role")
public class ServerRoleRestController extends RestControllerTemplate {

    @Autowired
    public void setGovernor(GovernorOfServerRole governor) {
        this.governor = governor;
    }

    @RequestMapping(value = "/{serverRoleId}", method = RequestMethod.GET)
    public ServerRole readOne(@PathVariable String serverRoleId) {
        return (ServerRole) processReadOneQuery(serverRoleId);
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<? extends Resource> readAll() {
        return processReadAllQuery();
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<?> create (@RequestBody ServerRole serverRole) throws ParameterValidateException {
        return processCreateQuery(serverRole);
    }

    @RequestMapping(value = "/{serverRoleId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<?> update(@PathVariable String serverRoleId, @RequestBody ServerRole serverRole) throws ParameterValidateException {
        return processUpdateQuery(serverRoleId, serverRole);
    }

    @RequestMapping(value = "/{serverRoleId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable String serverRoleId) {
        return processDeleteQuery(serverRoleId);
    }
}
