package ru.majordomo.hms.rc.staff.test.config;

//import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;

import com.mongodb.ServerAddress;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.net.InetSocketAddress;
import java.util.UUID;

@Configuration
@EnableMongoRepositories({"ru.majordomo.hms.rc.staff.repositories"})
public class RepositoriesConfig extends AbstractMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "rc-staff-" + ObjectId.get().toString();
    }

    @Bean(destroyMethod="shutdown")
    public MongoServer mongoServer() {
        MongoServer mongoServer = new MongoServer(new MemoryBackend());
        mongoServer.bind();
        return mongoServer;
    }

    @Override
    public MongoClient mongoClient() {
        return new MongoClient(new ServerAddress(mongoServer().getLocalAddress()));
//        MongoServer server = new MongoServer(new MemoryBackend());
//        InetSocketAddress serverAddress = server.bind();
//        MongoClient client = new MongoClient(new ServerAddress(serverAddress));
////        return new Fongo(getDatabaseName()).getMongo();
//        return client;
    }

    @Override
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(new SimpleMongoDbFactory(mongoClient(), UUID.randomUUID().toString()));
    }
}
