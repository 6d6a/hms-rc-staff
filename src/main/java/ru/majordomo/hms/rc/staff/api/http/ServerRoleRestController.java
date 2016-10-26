package ru.majordomo.hms.rc.staff.api.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServerRole;
import ru.majordomo.hms.rc.staff.repositories.ServerRoleRepository;
import ru.majordomo.hms.rc.staff.resources.ServerRole;

@RestController
@RequestMapping(value = "/${spring.application.name}/server-role")
public class ServerRoleRestController {

    private ServerRoleRepository repository;
    private GovernorOfServerRole governor;

    @Autowired
    public void setGovernor(GovernorOfServerRole governor) {
        this.governor = governor;
    }

    @Autowired
    public void setRepository(ServerRoleRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(value = "/{serverRoleId}", method = RequestMethod.GET)
    public ServerRole readOne(@PathVariable String serverRoleId) {
        return (ServerRole) governor.build(serverRoleId);
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<ServerRole> readAll() {
        List<ServerRole> serverRoles = new ArrayList<>();
        for (ServerRole serverRole : repository.findAll()) {
            serverRoles.add((ServerRole) governor.build(serverRole.getId()));
        }
        return serverRoles;
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<?> create (@RequestBody ServerRole serverRole) throws ParameterValidateException {
        governor.isValid(serverRole);
        ServerRole createdServerRole = repository.save(serverRole);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdServerRole.getId()).toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{serverRoleId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<?> update(@PathVariable String serverRoleId, @RequestBody ServerRole serverRole) throws ParameterValidateException {
        governor.isValid(serverRole);
        ServerRole storedServerRole = repository.findOne(serverRoleId);
        if (storedServerRole == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        serverRole.setId(storedServerRole.getId());
        repository.save(serverRole);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{serverRoleId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable String serverRoleId) {
        ServerRole storedServerRole = repository.findOne(serverRoleId);
        if (storedServerRole == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        repository.delete(serverRoleId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
