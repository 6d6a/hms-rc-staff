package ru.majordomo.hms.rc.staff.common;

import ru.majordomo.hms.rc.staff.config.RabbitMqConfig;

public enum ResourceAction {
    CREATE("." + RabbitMqConfig.Exchanges.Command.CREATE, "Создание"),
    DELETE("." + RabbitMqConfig.Exchanges.Command.DELETE, "Удаление"),
    UPDATE("." + RabbitMqConfig.Exchanges.Command.UPDATE, "Обновление");

    private final String exchangeSuffix;
    private final String actionName;

    ResourceAction(String exchangeSuffix, String actionName) {
        this.exchangeSuffix = exchangeSuffix;
        this.actionName = actionName;
    }

    public String getExchangeSuffix() {
        return exchangeSuffix;
    }

    public String getActionName() {
        return actionName;
    }
}