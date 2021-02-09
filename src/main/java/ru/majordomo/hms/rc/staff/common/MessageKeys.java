package ru.majordomo.hms.rc.staff.common;

/** Ключи для REST и RabbitMQ сообщений между сервисами */
public class MessageKeys {
    public static final String UNIX_ACCOUNT_HOMEDIR = "unixAccountHomedir";

    /**
     * обновление происходит из-за события на стороне rc-staff.
     * Например обновили в базе данных объект Template, Service и т.д.
     */
    public static final String STAFF_UPDATE = "staffUpdate";

    /** специальный ключ для TE */
    public static final String ISOLATED = "isolated";

    public static final String RESOURCE_ID = "resourceId";
}
