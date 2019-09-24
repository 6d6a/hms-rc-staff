package ru.majordomo.hms.rc.staff.event.service.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.majordomo.hms.rc.staff.event.template.TemplateUpdatedEvent;

@Component
public class ServiceEventListener {
    @EventListener
    @Async("threadPoolTaskExecutor")
    public void templateUpdated(TemplateUpdatedEvent event) {
        String templateId = event.getSource();
        //TODO пульнуть в очередь апдейты по сервисам с этим темплэйтом
    }
}
