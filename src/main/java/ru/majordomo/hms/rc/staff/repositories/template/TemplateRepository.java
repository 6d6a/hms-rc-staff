package ru.majordomo.hms.rc.staff.repositories.template;

import org.springframework.stereotype.Repository;
import ru.majordomo.hms.rc.staff.repositories.ResourceRepository;
import ru.majordomo.hms.rc.staff.resources.template.Template;

import java.util.List;

@Repository
public interface TemplateRepository extends BasicTemplateRepository<Template> {
}
