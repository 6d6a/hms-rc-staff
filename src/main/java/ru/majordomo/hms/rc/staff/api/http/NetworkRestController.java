package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

import ru.majordomo.hms.rc.staff.managers.GovernorOfNetwork;
import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.resources.Network;

@RestController
@CrossOrigin("*")
@RequestMapping("/${spring.application.name}/network")
public class NetworkRestController {
    @Autowired
    NetworkRepository networkRepository;

    @RequestMapping(value = "/{networkId}", method = RequestMethod.GET)
    public Network readOne(@PathVariable String networkId) {
        Network network = networkRepository.findOne(networkId);
        return network;
    }

    @RequestMapping(value = {"","/"}, method = RequestMethod.GET)
    public Collection<Network> readAll() {
        List<Network> networks = networkRepository.findAll();
        return networks;
    }

}
