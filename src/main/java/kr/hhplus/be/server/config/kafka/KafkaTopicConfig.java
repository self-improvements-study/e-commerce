package kr.hhplus.be.server.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic paymentSuccessTopic() {
        return TopicBuilder.name("order.v1.completed")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic couponIssueTopic() {
        return TopicBuilder.name("coupon.v1.issue")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
