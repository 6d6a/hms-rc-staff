package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;
import java.util.List;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfNetwork;
import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.resources.Network;

@RestController
@CrossOrigin("*")
@RequestMapping("/${spring.application.name}/network")
public class NetworkRestController {
    private NetworkRepository networkRepository;
    private GovernorOfNetwork governor;

    @Autowired
    public void setRepository(NetworkRepository repository) {
        this.networkRepository = repository;
    }

    @Autowired
    public void setGovernor(GovernorOfNetwork governor) {
        this.governor = governor;
    }

    @RequestMapping(value = "/{networkId}", method = RequestMethod.GET)
    public Network readOne(@PathVariable String networkId) {
        Network network = networkRepository.findOne(networkId);
        return network;
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<Network> readAll() {
        List<Network> networks = networkRepository.findAll();
        return networks;
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<?> create (@RequestBody Network network) throws ParameterValidateException {
        governor.isValid(network);
        Network createdNetwork = networkRepository.save(network);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdNetwork.getId()).toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{networkId}", method = RequestMethod.PATCH)
    public ResponseEntity<?> update(@PathVariable String networkId, @RequestBody Network network) throws ParameterValidateException {
        governor.isValid(network);
        Network storedNetwork = networkRepository.findOne(networkId);
        if (storedNetwork == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        network.setId(storedNetwork.getId());
        networkRepository.save(network);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{networkId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable String networkId) {
        Network storedNetwork = networkRepository.findOne(networkId);
        if (storedNetwork == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        networkRepository.delete(networkId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
