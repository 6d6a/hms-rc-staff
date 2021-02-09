package ru.majordomo.hms.rc.staff.event.service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.majordomo.hms.rc.staff.api.amqp.ServiceAmqpController;
import ru.majordomo.hms.rc.staff.event.socket.SocketUpdateEvent;
import ru.majordomo.hms.rc.staff.event.template.TemplateUpdatedEvent;
import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.resources.Service;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@Slf4j
@Component
@ParametersAreNonnullByDefault
@RequiredArgsConstructor
public class ServiceEventListener {
    private final ServiceAmqpController serviceAmqpController;
    private final ServiceRepository serviceRepository;

    @EventListener
    @Async("threadPoolTaskExecutor")
    public void templateUpdated(TemplateUpdatedEvent event) {
        String templateId = event.getSource();
        List<Service> services = serviceRepository.findServicesByTemplateId(templateId);
        sendUpdateToServices(services);
    }

    @EventListener
    @Async("threadPoolTaskExecutor")
    public void socketUpdated(SocketUpdateEvent event) {
        String socketId = event.getSource();
        List<Service> services = serviceRepository.findServicesBySocketIds(socketId);
        sendUpdateToServices(services);
    }

    private void sendUpdateToServices(List<Service> services) {
        for (Service service : services) {
            try {
                serviceAmqpController.sendStaffToTEUpdate(service);
            } catch (Exception e) {
                log.error("We got exception when send staffUpdate to TE message for service: " + service.getId(), e);
            }
        }
    }
}
