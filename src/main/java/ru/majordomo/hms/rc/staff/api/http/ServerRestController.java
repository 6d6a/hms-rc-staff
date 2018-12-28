package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ru.majordomo.hms.rc.staff.annotation.SecurityView;
import ru.majordomo.hms.rc.staff.annotation.SecurityView.View;
import ru.majordomo.hms.rc.staff.common.Views;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServer;
import ru.majordomo.hms.rc.staff.resources.Server;
import ru.majordomo.hms.rc.staff.resources.Storage;

@RestController
@RequestMapping(value = "/server")
public class ServerRestController extends RestControllerTemplate<Server> {
    @Autowired
    public void setGovernor(GovernorOfServer governor) {
        this.governor = governor;
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_VIEW')")
    @GetMapping("/{serverId}")
    public Server readOne(@PathVariable String serverId) {
        return processReadOneQuery(serverId);
    }

    //Возвращает ActiveMailboxStorage
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/{serverId}/active-storage")
    public Storage readActiveMailboxStorage(@PathVariable String serverId) {
        Map<String,String> requestParams = new HashMap<>();
        requestParams.put("server-id", serverId);
        requestParams.put("active-storage", "true");
        Server server = processReadOneWithParamsQuery(requestParams);
        return server.getActiveMailboxStorage();
    }

    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @GetMapping({"", "/"})
    @SecurityView(
            value = @View(showAll = true, authorities = {"ROLE_ADMIN", "SERVER_VIEW"}),
            fallback = @View(Views.Operator.class)
    )
    public Collection<Server> readAll(@RequestParam(required=false) Map<String,String> requestParams) {
        if (!requestParams.isEmpty()) {
            return processReadAllWithParamsQuery(requestParams);
        } else {
            return processReadAllQuery();
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_VIEW')")
    @GetMapping(value = {"", "/"}, headers = "X-HMS-Pageable=true")
    public Page<Server> readAll(
            @RequestParam(required=false) Map<String,String> requestParams,
            Pageable pageable
    ) {
        if (!requestParams.isEmpty()) {
            return processReadAllWithParamsQuery(requestParams, pageable);
        } else {
            return processReadAllQuery(pageable);
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_VIEW')")
    @GetMapping(value = {"", "/"}, headers = "X-HMS-Projection=OnlyIdAndName")
    public Collection<Server> readAllOnlyIdAndName(
            @RequestParam(required=false) Map<String,String> requestParams
    ) {
        if (!requestParams.isEmpty()) {
            return processReadAllWithParamsQueryOnlyIdAndName(requestParams);
        } else {
            return processReadAllQueryOnlyIdAndName();
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_VIEW')")
    @GetMapping("/filter")
    public Server readOneFilter(@RequestParam() Map<String,String> requestParams) {
        return processReadOneWithParamsQuery(requestParams);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_CREATE')")
    @PostMapping({"", "/"})
    public ResponseEntity<Server> create (@RequestBody Server server) throws ParameterValidateException {
        return processCreateQuery(server);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_EDIT')")
    @RequestMapping(value = "/{serverId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<Server> update(@PathVariable String serverId, @RequestBody Server server) throws ParameterValidateException {
        return processUpdateQuery(serverId, server);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('SERVER_DELETE')")
    @DeleteMapping("/{serverId}")
    public ResponseEntity<Void> delete(@PathVariable String serverId) {
        return processDeleteQuery(serverId);
    }
}
