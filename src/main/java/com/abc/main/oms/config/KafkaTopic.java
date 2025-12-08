package com.abc.main.oms.config;

public record KafkaTopic(String name, int numberOfPartition, int numberOfReplica) {
}
