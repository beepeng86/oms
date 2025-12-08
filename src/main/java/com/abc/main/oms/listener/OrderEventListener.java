package com.abc.main.oms.listener;

import com.abc.main.common.model.OrderCommandResult;
import com.abc.main.common.model.OrderUpdate;
import com.abc.main.oms.service.OrderCommandResultService;
import com.abc.main.oms.service.OrderUpdateService;
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
        topics = "${oms.kafka.topics.event.name}")
@Slf4j
@RequiredArgsConstructor
public class OrderEventListener {
    private final OrderUpdateService orderUpdateService;
    private final OrderCommandResultService orderCommandResultService;
    @KafkaHandler
    public void listen(OrderUpdate payload,
                       @Header(KafkaHeaders.RECEIVED_KEY) String key,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                       @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts) {
        log.debug("Received OrderUpdate {}, ts: {}", payload, ts);
        orderUpdateService.processOrder(payload);
    }

    @KafkaHandler
    public void listen(OrderCommandResult payload,
                       @Header(KafkaHeaders.RECEIVED_KEY) String key,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                       @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts) {
        log.debug("Received OrderCommandResult: {} ts: {} ", payload, ts);
        orderCommandResultService.processOrder(payload);
    }
}
