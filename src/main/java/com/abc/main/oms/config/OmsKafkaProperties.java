package com.abc.main.oms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "oms.kafka")
public record OmsKafkaProperties(
        Map<String, KafkaTopic> topics,
        Map<String, Account> accounts,
        // TODO: have to reconsider should we retrieve this from algo param etc
        String symbol
) {}
