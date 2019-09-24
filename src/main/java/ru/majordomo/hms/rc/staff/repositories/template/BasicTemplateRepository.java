package ru.majordomo.hms.rc.staff.repositories.template;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;
import ru.majordomo.hms.rc.staff.repositories.ResourceRepository;
import ru.majordomo.hms.rc.staff.resources.template.Template;

import java.util.List;

@NoRepositoryBean
public interface BasicTemplateRepository<T extends Template> extends ResourceRepository<T, String> {
    List<T> findByAvailableToAccounts(Boolean availableToAccounts);
    T findByIdAndAvailableToAccounts(String id, Boolean availableToAccounts);
    T findFirstByMigratedServiceTemplateIds(String serviceTemplateId);
}
