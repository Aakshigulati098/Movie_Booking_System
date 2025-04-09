package com.example.movie_booking_system.config;


import com.example.movie_booking_system.dto.AuctionResultDTO;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public KafkaAdmin kafkaAdmin() {

        Map<String,Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);

    }

    @Bean
    public NewTopic auctionEndTopic() {
        return TopicBuilder.name("auction.end")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {

        Map<String,Object> configs = new HashMap<>();


//        this is to provide the server on which the kafka server is running
//        as mai docker se chala rha hu mera local pe chal rha hai so i would be needing something to run in cloud
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        abhi produce kar rha hu kafka ko thori pata hai kaise data ko lena hai uska kam toh data stream karna hai
//        so appa karenge seriealize or deserialize karne ka kam ab producer serialize karega and consumer deserialize karega
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(configs);

    }

//    abb springboot hai bhai ne template pattern se rishta mast banake rakha hai so yes
//    kafka ka bhi template hai matlab another REASON to love lld
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, AuctionResultDTO> consumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(AuctionResultDTO.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AuctionResultDTO> kafkaListenerContainerFactory(
            ConsumerFactory<String, AuctionResultDTO> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, AuctionResultDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }
}
