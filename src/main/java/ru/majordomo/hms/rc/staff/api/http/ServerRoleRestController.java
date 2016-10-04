package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import ru.majordomo.hms.rc.staff.repositories.ServerRoleRepository;
import ru.majordomo.hms.rc.staff.resources.ServerRole;

@RestController
@RequestMapping(value = "/${spring.application.name}/server-role")
public class ServerRoleRestController {
    ServerRoleRepository repository;

    @Autowired
    public void setRepository(ServerRoleRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(value = "/{serverRoleId}", method = RequestMethod.GET)
    public ServerRole readOne(@PathVariable String serverRoleId) {
        return repository.findOne(serverRoleId);
    }

    @RequestMapping(value = {"","/"}, method = RequestMethod.GET)
    public Collection<ServerRole> readAll() {
        return repository.findAll();
    }
}
