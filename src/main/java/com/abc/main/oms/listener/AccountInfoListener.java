package com.abc.main.oms.listener;

import com.abc.main.common.model.AccountInfo;
import com.abc.main.oms.service.ReconcilationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(groupId = "${oms.kafka.group-id}",
        containerFactory = "kafkaListenerContainerFactory",
        topics = "${oms.kafka.topics.account-info.name}")
@Slf4j
@RequiredArgsConstructor
public class AccountInfoListener {
    private final ReconcilationService reconcilationService;
    @KafkaHandler
    public void listen(AccountInfo payload,
                       @Header(KafkaHeaders.RECEIVED_KEY) String key,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                       @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts) {
        reconcilationService.updateAccountInfo(payload);
    }
}
