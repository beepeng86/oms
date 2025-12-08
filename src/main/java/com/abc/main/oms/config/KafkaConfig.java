package com.abc.main.oms.config;

import com.abc.main.common.model.AccountInfo;
import com.abc.main.common.model.OrderCommandResult;
import com.abc.main.common.model.OrderProposal;
import com.abc.main.common.model.OrderUpdate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.utils.Bytes;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.BytesJsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class KafkaConfig {

    @Bean
    public CommonErrorHandler errorHandler(KafkaProperties kafkaProperties) {
        return new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(errorKafkaTemplate(kafkaProperties)), new FixedBackOff(100L, 2));
    }

    @Bean
    public ConsumerFactory<String, Bytes> consumerFactory(KafkaProperties kafkaProperties) {
        BytesDeserializer deser = new BytesDeserializer();

        return new DefaultKafkaConsumerFactory<>(
                kafkaProperties.buildConsumerProperties(null),
                new StringDeserializer(),
                deser
        );
    }

    private KafkaTemplate<String, Object> errorKafkaTemplate(KafkaProperties kafkaProperties) {
        DefaultKafkaProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(
            kafkaProperties.buildProducerProperties(null));
        return new KafkaTemplate<>(factory,
            Collections.singletonMap(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BytesSerializer.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Bytes> kafkaListenerContainerFactory(
            KafkaProperties kafkaProperties) {
        ConcurrentKafkaListenerContainerFactory<String, Bytes> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(kafkaProperties));
        factory.setRecordMessageConverter(converter());
        factory.setCommonErrorHandler(errorHandler(kafkaProperties));

        return factory;
    }

    @Bean
    public RecordMessageConverter converter() {
        BytesJsonMessageConverter converter = new BytesJsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);
        typeMapper.addTrustedPackages("com.abc.maincommon.*");
        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put("orderCommonResult", OrderCommandResult.class);
        mappings.put("orderUpdate", OrderUpdate.class);
        mappings.put("orderProposal", OrderProposal.class);
        mappings.put("accountInfo", AccountInfo.class);
        typeMapper.setIdClassMapping(mappings);
        converter.setTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public NewTopic orderCommandTopic(OmsKafkaProperties prop) {
        KafkaTopic topic = prop.topics().get("command");
        return TopicBuilder.name(topic.name())
                .partitions(topic.numberOfPartition())
                .replicas(topic.numberOfReplica())
                .build();
    }

    @Bean
    public NewTopic orderEventTopic(OmsKafkaProperties prop) {
        KafkaTopic topic = prop.topics().get("event");
        return TopicBuilder.name(topic.name())
                .partitions(topic.numberOfPartition())
                .replicas(topic.numberOfReplica())
                .build();
    }

    @Bean
    public NewTopic orderProposalTopic(OmsKafkaProperties prop) {
        KafkaTopic topic = prop.topics().get("proposal");
        return TopicBuilder.name(topic.name())
                .partitions(topic.numberOfPartition())
                .replicas(topic.numberOfReplica())
                .build();
    }

    @Bean
    public NewTopic accountInfoTopic(OmsKafkaProperties prop) {
        KafkaTopic topic = prop.topics().get("account-info");
        return TopicBuilder.name(topic.name())
            .partitions(topic.numberOfPartition())
            .replicas(topic.numberOfReplica())
            .build();
    }
}
