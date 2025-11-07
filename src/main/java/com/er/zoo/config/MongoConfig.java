package com.er.zoo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
/**
 * MongoDB configuration class for the Zoo API.
 * <p>
 * This class enables auditing support for MongoDB entities, allowing automatic
 * population of auditing fields such as {@code @CreatedDate} and {@code @LastModifiedDate}.
 * </p>
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
