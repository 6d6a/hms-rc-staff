package ru.majordomo.hms.rc.staff.repositories;

import org.springframework.stereotype.Repository;

import ru.majordomo.hms.rc.staff.resources.Service;

import java.util.List;

@Repository
public interface ServiceRepository extends ResourceRepository<Service, String> {
    List<Service> findByAccountId(String accountId);
    List<Service> findByAccountIdAndServerId(String accountId, String serverId);
    List<Service> findByServerIdAndAccountIdNull(String serverId);
    List<Service> findByServerId(String serverId);
    boolean existsByTemplateId(String templateId);
    boolean existsByAccountIdAndTemplateId(String accountId, String templateId);
    List<Service> findByAccountIdAndTemplateId(String accountId, String templateId);
}
