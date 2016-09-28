package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import ru.majordomo.hms.rc.staff.repositories.StorageRepository;
import ru.majordomo.hms.rc.staff.resources.Storage;

@RestController
@RequestMapping("/${spring.application.name}/storage")
@CrossOrigin("*")
public class StorageRestController {
    @Autowired
    StorageRepository repository;

    @RequestMapping(value = "/{storageId}", method = RequestMethod.GET)
    public Storage readOne(@PathVariable String storageId) {
        return repository.findOne(storageId);
    }

    @RequestMapping(value = {"","/"}, method = RequestMethod.GET)
    public Collection<Storage> readAll() {
        return repository.findAll();
    }
}
