package ru.majordomo.hms.rc.staff.api.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;

import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfStorage;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.resources.Storage;

@RestController
@RequestMapping("/storage")
@CrossOrigin("*")
public class StorageRestController extends RestControllerTemplate {

    @Autowired
    public void setGovernor(GovernorOfStorage governor) {
        this.governor = governor;
    }

    @RequestMapping(value = "/{storageId}", method = RequestMethod.GET)
    public Storage readOne(@PathVariable String storageId) {
        return (Storage) processReadOneQuery(storageId);
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Collection<? extends Resource> readAll() {
        return processReadAllQuery();
    }

    @RequestMapping(value = {"", "/"}, method = RequestMethod.POST)
    public ResponseEntity<?> create (@RequestBody Storage storage) throws ParameterValidateException {
        return processCreateQuery(storage);
    }

    @RequestMapping(value = "/{storageId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<?> update(@PathVariable String storageId, @RequestBody Storage storage) throws ParameterValidateException {
        return processUpdateQuery(storageId, storage);
    }

    @RequestMapping(value = "/{storageId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> delete(@PathVariable String storageId) {
        return processDeleteQuery(storageId);
    }
}
