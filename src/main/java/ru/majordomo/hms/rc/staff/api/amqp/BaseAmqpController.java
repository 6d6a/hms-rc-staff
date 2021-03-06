package ru.majordomo.hms.rc.staff.api.amqp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.majordomo.hms.personmgr.exception.InternalApiException;
import ru.majordomo.hms.personmgr.exception.ParameterValidationException;
import ru.majordomo.hms.rc.staff.api.dto.Error;
import ru.majordomo.hms.rc.staff.api.clients.Sender;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.common.MessageKeys;
import ru.majordomo.hms.rc.staff.common.ResourceAction;
import ru.majordomo.hms.rc.staff.common.ResourceActionContext;
import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.managers.GovernorOfServer;
import ru.majordomo.hms.rc.staff.managers.LordOfResources;
import ru.majordomo.hms.rc.staff.resourceProcessor.ResourceProcessor;
import ru.majordomo.hms.rc.staff.resourceProcessor.ResourceProcessorContext;
import ru.majordomo.hms.rc.staff.resourceProcessor.impl.*;
import ru.majordomo.hms.rc.staff.resourceProcessor.impl.te.TeCreateProcessor;
import ru.majordomo.hms.rc.staff.resourceProcessor.impl.te.TeDeleteProcessor;
import ru.majordomo.hms.rc.staff.resourceProcessor.impl.te.TeUpdateProcessor;
import ru.majordomo.hms.rc.staff.resourceProcessor.support.ResourceByUrlBuilder;
import ru.majordomo.hms.rc.staff.resourceProcessor.support.ResultSender;
import ru.majordomo.hms.rc.staff.resourceProcessor.support.RoutingKeyResolver;
import ru.majordomo.hms.rc.staff.resourceProcessor.support.impl.DefaultResourceByUrlBuilder;
import ru.majordomo.hms.rc.staff.resources.ConnectableToAccount;
import ru.majordomo.hms.rc.staff.resources.ConnectableToServer;
import ru.majordomo.hms.rc.staff.resources.Resource;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.*;
import java.util.stream.Collectors;

import static ru.majordomo.hms.rc.staff.common.Constants.PM;
import static ru.majordomo.hms.rc.staff.common.Constants.TE;

@Component
@EnableRabbit
@ParametersAreNonnullByDefault
abstract class BaseAmqpController<T extends Resource> implements ResourceProcessorContext<T> {
    private String applicationName;
    private String instanceName;

    private Sender sender;
    private GovernorOfServer governorOfServer;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected ResourceProcessor<T> getEventProcessor(ResourceActionContext<T> context) {
        switch (context.getEventProvider()) {
            case TE:
                switch (context.getAction()) {
                    case CREATE:
                        return new TeCreateProcessor<>(this);
                    case UPDATE:
                        return new TeUpdateProcessor<>(this);
                    case DELETE:
                        return new TeDeleteProcessor<>(this);
                }
            case PM:
                switch (context.getAction()) {
                    case CREATE:
                        return new DefaultCreatePmProcessor<>(this);
                    case UPDATE:
                        return new DefaultUpdatePmProcessor<>(this);
                    case DELETE:
                        return new DefaultDeletePmProcessor<>(this);
                }
        }
        return null;
    }

    protected LordOfResources<T> governor;

    public abstract String getResourceType();

    public final RoutingKeyResolver<T> getRoutingKeyResolver() {
        return this::getRoutingKey;
    }

    protected abstract String getRoutingKey(ResourceActionContext<T> context);

    public final ResourceByUrlBuilder<T> getResourceByUrlBuilder() {
        return new DefaultResourceByUrlBuilder<>(governor);
    }

    protected String getDefaultRoutingKey() { return PM; }

    @Value("${spring.application.name}")
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Value("${hms.instance.name}")
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    @Autowired
    public void setSender(Sender sender) {
        this.sender = sender;
    }

    @Override
    public final LordOfResources<T> getGovernor() {
        return governor;
    }

    @Override
    public final ResultSender<T> getSender() {
        return this::sendResult;
    }

    @Autowired
    public void setGovernorOfServer(GovernorOfServer governorOfServer) {
        this.governorOfServer = governorOfServer;
    }

    private void handleEvent(String eventProvider, ServiceMessage serviceMessage, ResourceAction action) {
        ResourceActionContext<T> context = new ResourceActionContext<>(serviceMessage, action);
        try {
            context.setEventProvider(
                    getRealProviderName(eventProvider)
            );

            ResourceProcessor<T> eventProcessor = getEventProcessor(context);

            if (eventProcessor != null) {
                eventProcessor.process(context);
            } else {
                log.error("ResourceEventProcessor for {} is null, context: {}", getResourceType(), context);
                throw new InternalApiException("?????????????????? ??????????????  ???? ??????????????");
            }

        } catch (Exception e) {
            processException(context, e);
        }
    }

    void handleCreate(String eventProvider, ServiceMessage serviceMessage) {
        handleEvent(eventProvider, serviceMessage, ResourceAction.CREATE);
    }

    void handleUpdate(String eventProvider, ServiceMessage serviceMessage) {
        handleEvent(eventProvider, serviceMessage, ResourceAction.UPDATE);
    }

    void handleDelete(String eventProvider, ServiceMessage serviceMessage) {
        handleEvent(eventProvider, serviceMessage, ResourceAction.DELETE);
    }

    protected ServiceMessage createReportMessage(
            ResourceActionContext<T> context
    ) {
        T resource = context.getResource();
        ServiceMessage event = context.getMessage();

        ServiceMessage report = new ServiceMessage();
        report.setActionIdentity(event.getActionIdentity());
        report.setOperationIdentity(event.getOperationIdentity());
        report.setAccountId(event.getAccountId());
        if (resource != null) {
            report.setObjRef(
                    getObjRef(resource)
            );
            report.addParam("name", resource.getName());

            if (resource instanceof ConnectableToAccount && (report.getAccountId() == null || report.getAccountId().equals(""))) {
                report.setAccountId(((ConnectableToAccount) resource).getAccountId());
            }
        }
        Boolean eventSuccess = (Boolean) event.getParam("success");
        if (eventSuccess == null) {
            report.addParam("success", true);
        } else {
            report.addParam("success", eventSuccess);
        }


        if (event.getParam("teParams") != null) {
            Map<String, Object> teParams = null;
            ObjectMapper mapper = new ObjectMapper();
            try {
                teParams = mapper.convertValue(event.getParam("teParams"), new TypeReference<Map<String, Object>>() {});
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                log.error("Error in converting teParams from message. Exception: " + e.getMessage());
            }

            if (teParams != null) {
                for (Map.Entry<String, Object> teParam: teParams.entrySet()) {
                    report.addParam(teParam.getKey(), teParam.getValue());
                }
            }
        }

        Arrays.asList("errors", "exceptionClass", "errorMessage").forEach(key -> {
            if (event.getParam(key) != null) {
                report.addParam(key, event.getParam(key));
            }
        });

        return report;
    }

    private String getObjRef(T resource) {
        return "http://" + applicationName + "/" + getResourceType() + "/" + resource.getId();
    }

    protected String getTaskExecutorRoutingKey(T resource) throws ParameterValidationException, InternalApiException, ResourceNotFoundException {
        try {
            String serverName;
            if (resource instanceof ConnectableToServer) {
                ConnectableToServer connectableToServer = (ConnectableToServer) resource;
                serverName = governorOfServer.build(connectableToServer.getServerId()).getName();
            } else {
                throw new InternalApiException("resource is not instanceof ConnectableToServer");
            }

            return TE + "." + serverName.split("\\.")[0];
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[getTaskExecutorRoutingKey] got exception: {} ; resource id: {} class: {}",
                    e.getMessage(), resource.getId(), resource.getClass().getSimpleName());
            throw new InternalApiException("Exception: " + e.getMessage());
        }
    }

    private String getRealProviderName(String eventProvider) {
        return eventProvider.replaceAll("^" + instanceName + "\\.", "");
    }

    private void sendToAmqp(String exchange, String routingKey, ServiceMessage payload) {
        sender.send(exchange, routingKey, payload);
    }

    private void processException(ResourceActionContext<T> context, Exception e) {
        ServiceMessage serviceMessage = context.getMessage();

        String errorMessage;

        Collection<Error> errors;

        if (e instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) e;

            errors = getErrors(cve);

            errorMessage = cve.getConstraintViolations()
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining());

        } else if (e instanceof ParameterValidationException) {
            errorMessage = "?????????????????? ?????????????? ???? ??????????????: " + e.getMessage();
            errors = Collections.singletonList(new Error(getResourceType(), Collections.singletonList(e.getMessage())));
        } else {
            errorMessage = "?????????????????? ?????????????? ???? ??????????????";
            errors = Collections.singletonList(new Error(getResourceType(), Collections.singletonList(e.getMessage())));
        }

        serviceMessage.addParam("exceptionClass", e.getClass().getSimpleName());
        serviceMessage.addParam("errors", errors);

        sendErrorToAmqp(context, errorMessage);
    }

    private void sendErrorToAmqp(ResourceActionContext<T> context, String errorMessage) {
        ServiceMessage serviceMessage = context.getMessage();

        ResourceAction action = context.getAction();

        log.error("ACTION_IDENTITY: {} OPERATION_IDENTITY: {} {} ?????????????? {} ???? ??????????????: {}",
                serviceMessage.getActionIdentity(), serviceMessage.getOperationIdentity(), action.getActionName(),
                getResourceType(), errorMessage);

        serviceMessage.addParam("success", false);

        serviceMessage.addParam("errorMessage", errorMessage);

        sendResult(context, getDefaultRoutingKey());
    }

    private void sendResult(ResourceActionContext<T> context, String routingKey) {
        ResourceAction action = context.getAction();

        ServiceMessage report = createReportMessage(context);

        String exchange = getResourceType() + action.getExchangeSuffix();

        sendToAmqp(exchange, routingKey, report);
    }

    private Collection<Error> getErrors(ConstraintViolationException e) {
        Map<Path, Error> errors = new HashMap<>();
        e.getConstraintViolations()
                .forEach(c -> {
                    Error error = errors.getOrDefault(c.getPropertyPath(), new Error(c.getPropertyPath().toString(), new ArrayList<>()));
                    error.getErrors().add(c.getMessage());
                    errors.put(c.getPropertyPath(), error);
                });
        return errors.values();
    }

    /**
     * ???????????? ?????????????????? ?? TE ?? ?????? ?????? ???????????????????? ???????????????? ???????????? ????-???? ?????????????????? ???????????????????????? ???? ?????????????? rc-staff
     *
     * todo ?????????? ?????????????????? ?????????? ?????????????????????????? ????????????, ?????????????????? ???????????????????? ???????? ???? ???????????????????????? ?? ?????????? ???????????????????? ?? ??????????
     * @param resource
     */
    public void sendStaffToTEUpdate(T resource) {
        ServiceMessage message = new ServiceMessage();
        message.setObjRef(getObjRef(resource));
        message.addParam(MessageKeys.STAFF_UPDATE, true);
        message.addParam(MessageKeys.ISOLATED, true);

        String exchange = getResourceType() + ResourceAction.UPDATE.getExchangeSuffix();
        String routingKey = getTaskExecutorRoutingKey(resource);
        sendToAmqp(exchange, routingKey, message);
    }
}
