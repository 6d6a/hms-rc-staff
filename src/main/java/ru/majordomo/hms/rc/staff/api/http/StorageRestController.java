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

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfStorage;
import ru.majordomo.hms.rc.staff.repositories.StorageRepository;
import ru.majordomo.hms.rc.staff.resources.Storage;

@RestController
@RequestMapping("/${spring.application.name}/storage")
@CrossOrigin("*")
public class StorageRestController {

    private StorageRepository repository;
    private GovernorOfStorage governor;

    @Autowired
    public void setRepository(StorageRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setGovernor(GovernorOfStorage governor) {
        this.governor = governor;
    }

    @RequestMapping(value = "/{storageId}", method = RequestMethod.GET)
    public Storage readOne(@PathVariable String storageId) {
        return repository.findOne(storageId);
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<Storage> readAll() {
        return repository.findAll();
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<?> create (@RequestBody Storage storage) throws ParameterValidateException {
        governor.isValid(storage);
        Storage createdStorage = repository.save(storage);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdStorage.getId()).toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{storageId}", method = RequestMethod.PATCH)
    public ResponseEntity<?> update(@PathVariable String storageId, @RequestBody Storage storage) throws ParameterValidateException {
        governor.isValid(storage);
        Storage storedStorage = repository.findOne(storageId);
        if (storedStorage == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        storage.setId(storedStorage.getId());
        repository.save(storage);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/{storageId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable String storageId) {
        Storage storedStorage = repository.findOne(storageId);
        if (storedStorage == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        repository.delete(storageId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
