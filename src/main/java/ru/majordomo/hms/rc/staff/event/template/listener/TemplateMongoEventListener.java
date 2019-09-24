package ru.majordomo.hms.rc.staff.event.template.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.template.Template;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
public class TemplateMongoEventListener extends AbstractMongoEventListener<Template> {
    private final MongoOperations mongoOperations;

    @Autowired
    public TemplateMongoEventListener(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public void onAfterConvert(AfterConvertEvent<Template> event) {
        super.onAfterConvert(event);
        loadTransientProps(event.getSource());
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<Template> event) {
        super.onBeforeSave(event);
        loadTransientProps(event.getSource());
    }

    private void loadTransientProps(Template template) {
        if (!template.getConfigTemplateIds().isEmpty()) {
            template.setConfigTemplates(mongoOperations.find(new Query(where("_id").in(template.getConfigTemplateIds())), ConfigTemplate.class));
        }
    }
}
