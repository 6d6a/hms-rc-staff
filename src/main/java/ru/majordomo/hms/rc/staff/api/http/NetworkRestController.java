package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfNetwork;
import ru.majordomo.hms.rc.staff.resources.Network;

@RestController
@RequestMapping("/network")
public class NetworkRestController extends RestControllerTemplate<Network> {

    @Autowired
    public void setGovernor(GovernorOfNetwork governor) {
        this.governor = governor;
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('NETWORK_VIEW')")
    @RequestMapping(value = "/{networkId}", method = RequestMethod.GET)
    public Network readOne(@PathVariable String networkId) {
        return processReadOneQuery(networkId);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('NETWORK_VIEW')")
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<Network> readAll(@RequestParam(required=false, defaultValue="") String name) {
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

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('NETWORK_CREATE')")
    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<Network> create (@RequestBody Network network) throws ParameterValidateException {
        return processCreateQuery(network);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('NETWORK_EDIT')")
    @RequestMapping(value = "/{networkId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<Network> update(@PathVariable String networkId, @RequestBody Network network) throws ParameterValidateException {
        return processUpdateQuery(networkId, network);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('NETWORK_DELETE')")
    @RequestMapping(value = "/{networkId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable String networkId) {
        return processDeleteQuery(networkId);
    }
}
