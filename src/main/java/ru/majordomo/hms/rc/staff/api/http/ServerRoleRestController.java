package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServerRole;
import ru.majordomo.hms.rc.staff.resources.ServerRole;

@RestController
@RequestMapping(value = "/server-role")
public class ServerRoleRestController extends RestControllerTemplate<ServerRole> {

    @Autowired
    public void setGovernor(GovernorOfServerRole governor) {
        this.governor = governor;
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_ROLE_VIEW')")
    @RequestMapping(value = "/{serverRoleId}", method = RequestMethod.GET)
    public ServerRole readOne(@PathVariable String serverRoleId) {
        return processReadOneQuery(serverRoleId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_ROLE_VIEW')")
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<ServerRole> readAll(@RequestParam(required=false, defaultValue="") String name) {
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

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_ROLE_VIEW')")
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET, headers = "X-HMS-Pageable=true")
    public Page<ServerRole> readAll(
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

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_ROLE_VIEW')")
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET, headers = "X-HMS-Projection=OnlyIdAndName")
    public Collection<ServerRole> readAll() {
        return processReadAllQueryOnlyIdAndName();
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_ROLE_CREATE')")
    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<ServerRole> create (@RequestBody ServerRole serverRole) throws ParameterValidateException {
        return processCreateQuery(serverRole);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_ROLE_EDIT')")
    @RequestMapping(value = "/{serverRoleId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<ServerRole> update(@PathVariable String serverRoleId, @RequestBody ServerRole serverRole) throws ParameterValidateException {
        return processUpdateQuery(serverRoleId, serverRole);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_ROLE_DELETE')")
    @RequestMapping(value = "/{serverRoleId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable String serverRoleId) {
        return processDeleteQuery(serverRoleId);
    }
}
