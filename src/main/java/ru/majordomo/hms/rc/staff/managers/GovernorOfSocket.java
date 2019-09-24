package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import ru.majordomo.hms.rc.staff.common.IntegersContainer;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.socket.SocketRepository;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.socket.NetworkSocket;
import ru.majordomo.hms.rc.staff.resources.socket.Socket;
import ru.majordomo.hms.rc.staff.resources.validation.group.SocketChecks;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

@Component
public class GovernorOfSocket extends LordOfResources<Socket> {
    private static final int minPortForAccount = 10000;
    private static final int maxPortForAccount = 20000;

    private GovernorOfService governorOfService;
    private Validator validator;
    private MongoOperations mongoOperations;

    @Autowired
    public void setServiceSocketRepository(SocketRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setGovernorOfService(GovernorOfService governorOfService) {
        this.governorOfService = governorOfService;
    }

    @Autowired
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @Autowired
    public void setMongoOperations(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public void isValid(Socket socket) throws ParameterValidateException {
        Set<ConstraintViolation<Socket>> constraintViolations = validator.validate(socket, SocketChecks.class);

        if (!constraintViolations.isEmpty()) {
            logger.error("socket: " + socket + " constraintViolations: " + constraintViolations.toString());
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    @Override
    public Socket build(Map<String, String> keyValue) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public void preDelete(String resourceId) {
        boolean foundService = mongoOperations.exists(
                new Query(new Criteria("socketIds").is(resourceId)),
                Service.class
        );

        if (foundService) {
            throw new ParameterValidateException("Я нашла Service в котором используется удаляемый Socket");
        }
    }

    public Socket generateForAccount(String name) {
        MatchOperation match = Aggregation.match(
                Criteria.where("port").gte(minPortForAccount).lte(maxPortForAccount));

        GroupOperation group = Aggregation.group().addToSet("port").as("data");

        Aggregation aggregation = Aggregation.newAggregation(match, group);

        List<IntegersContainer> stringsContainers =
                mongoOperations.aggregate(aggregation, NetworkSocket.class, IntegersContainer.class).getMappedResults();

        NetworkSocket serviceSocket = new NetworkSocket();
        serviceSocket.setAddress("127.0.0.1");
        serviceSocket.setPort(minPortForAccount);
        serviceSocket.setName(name);
        serviceSocket.setProtocol("http");
        serviceSocket.setSwitchedOn(true);

        if (stringsContainers != null && !stringsContainers.isEmpty()) {
            stringsContainers.get(0).getData().sort(Comparator.naturalOrder());

            serviceSocket.setPort(
                    IntStream.range(minPortForAccount, maxPortForAccount)
                            .filter(port -> !stringsContainers.get(0).getData().contains(port))
                            .findFirst()
                            .orElse(minPortForAccount)
            );
        }

        isValid(serviceSocket);
        save(serviceSocket);

        return serviceSocket;
    }
}
