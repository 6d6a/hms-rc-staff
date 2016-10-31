package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import ru.majordomo.hms.rc.staff.resources.Resource;

@RestController
@CrossOrigin("*")
@RequestMapping("/network")
public class NetworkRestController extends RestControllerTemplate {

    @Autowired
    public void setGovernor(GovernorOfNetwork governor) {
        this.governor = governor;
    }

    @RequestMapping(value = "/{networkId}", method = RequestMethod.GET)
    public Network readOne(@PathVariable String networkId) {
        return (Network) processReadOneQuery(networkId);
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<? extends Resource> readAll(@RequestParam(required=false, defaultValue="") String name) {
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

    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<?> create (@RequestBody Network network) throws ParameterValidateException {
        return processCreateQuery(network);
    }

    @RequestMapping(value = "/{networkId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<?> update(@PathVariable String networkId, @RequestBody Network network) throws ParameterValidateException {
        return processUpdateQuery(networkId, network);
    }

    @RequestMapping(value = "/{networkId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable String networkId) {
        return processDeleteQuery(networkId);
    }
}
