package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import ru.majordomo.hms.rc.staff.resources.Resource;

import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
public interface ResourceRepository<T extends Resource, ID extends Serializable> extends MongoRepository<T, ID> {
    List<T> findAll();
    List<T> findByName(String name);
    Page<T> findAll(Pageable pageable);
    Page<T> findByName(String name, Pageable pageable);
    T findOneByName(String name);

    @Query(value="{}", fields="{name : 1}")
    List<T> findOnlyIdAndName();

    @Query(value="{'name' : ?0}", fields="{name : 1}")
    List<T> findByNameOnlyIdAndName(String name);

    @Query("{'name':{$regex:?0, $options: 'i'}}")
    List<T> findByNameRegEx(String name);

    @Query("{'name':{$regex:?0, $options: 'i'}}")
    Page<T> findByNameRegEx(String name, Pageable pageable);

    Long deleteByName(String name);
}
