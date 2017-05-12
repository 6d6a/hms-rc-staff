package ru.majordomo.hms.rc.staff.test.config;

import com.github.fakemongo.Fongo;
import com.mongodb.Mongo;

import com.mongodb.MongoClient;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.UUID;

@Configuration
@EnableMongoRepositories({"ru.majordomo.hms.rc.staff.repositories"})
public class RepositoriesConfig extends AbstractMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "rc-staff-" + ObjectId.get().toString();
    }

    @Override
    public MongoClient mongo() throws Exception {
        return new Fongo(getDatabaseName()).getMongo();
    }

    @Override
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(new SimpleMongoDbFactory(mongo(), UUID.randomUUID().toString()));
    }
}
