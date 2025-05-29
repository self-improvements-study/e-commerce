package kr.hhplus.be.server;

import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

@Configuration
public class KafkaTestContainersConfig {


    private static final KafkaContainer KAFKA_CONTAINER;

    static {
        KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"));
        KAFKA_CONTAINER.setPortBindings(List.of("9092:9092"));
        KAFKA_CONTAINER.start();

        System.setProperty("spring.kafka.bootstrap-servers", KAFKA_CONTAINER.getBootstrapServers());
    }

}
