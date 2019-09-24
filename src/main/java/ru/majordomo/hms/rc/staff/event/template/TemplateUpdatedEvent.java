package ru.majordomo.hms.rc.staff.event.template;

import org.springframework.context.ApplicationEvent;

public class TemplateUpdatedEvent extends ApplicationEvent {
    public TemplateUpdatedEvent(String id) {
        super(id);
    }

    @Override
    public String getSource() {
        return (String) super.getSource();
    }
}
