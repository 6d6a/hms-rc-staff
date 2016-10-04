package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import ru.majordomo.hms.rc.staff.repositories.ServerRepository;
import ru.majordomo.hms.rc.staff.resources.Server;

//TODO
@RestController
@CrossOrigin("*")
@RequestMapping(value = "/${spring.application.name}/server")
public class ServerRestController {
    @Autowired
    ServerRepository repository;

    @RequestMapping(value = "/{serverId}", method = RequestMethod.GET)
    public Server readOne(@PathVariable String serverId) {
        Server server = repository.findOne(serverId);
        return server;
    }

    @RequestMapping(value = {"/",""}, method = RequestMethod.GET)
    public Collection<Server> readAll() {
        return repository.findAll();
    }

//    @RequestMapping(value = {"/",""}, method = RequestMethod.POST)
//    public ResponseEntity<?> create(@RequestBody Server input) {
//
//    }
}
