package ru.majordomo.hms.rc.staff.event.socket;

import org.springframework.context.ApplicationEvent;

public class SocketUpdateEvent extends ApplicationEvent {
    public SocketUpdateEvent(String id) {
        super(id);
    }

    @Override
    public String getSource() {
        return (String) super.getSource();
    }
}
