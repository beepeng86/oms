package com.abc.main.oms.config;

public record Account(
    String accountKey,
    int partitionId,
    String category,
    String accountType,
    String name
) {}
