package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
public interface ResourceRepository<T, ID extends Serializable> extends MongoRepository<T, ID> {
    List<T> findAll();
    List<T> findByName(String name);
    Page<T> findAll(Pageable pageable);
    Page<T> findByName(String name, Pageable pageable);
    T findOneByName(String name);

    Long deleteByName(String name);
}
