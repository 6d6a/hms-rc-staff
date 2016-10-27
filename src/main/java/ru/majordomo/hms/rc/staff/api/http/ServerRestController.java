package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServer;
import ru.majordomo.hms.rc.staff.resources.Server;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/${spring.application.name}/server")
public class ServerRestController {

    private GovernorOfServer governor;

    @Autowired
    public void setGovernor(GovernorOfServer governor) {
        this.governor = governor;
    }

    @RequestMapping(value = "/{serverId}", method = RequestMethod.GET)
    public Server readOne(@PathVariable String serverId) {
        return (Server) governor.build(serverId);
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<Server> readAll() {
        return governor.build();
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<?> create (@RequestBody Server server) throws ParameterValidateException {
        governor.isValid(server);
        Server createdNetwork = (Server) governor.save(server);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdNetwork.getId()).toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{serverId}", method = RequestMethod.PATCH)
    public ResponseEntity<?> update(@PathVariable String serverId, @RequestBody Server server) throws ParameterValidateException {
        governor.isValid(server);
        try {
            Server storedServer = (Server) governor.build(serverId);
            server.setId(storedServer.getId());
            governor.save(server);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{serverId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable String serverId) {
        try {
            Server storedNetwork = (Server) governor.build(serverId);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        governor.delete(serverId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
