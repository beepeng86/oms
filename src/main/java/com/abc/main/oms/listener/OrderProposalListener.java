package com.abc.main.oms.listener;

import com.abc.main.common.model.OrderProposal;
import com.abc.main.oms.service.OrderProposalService;
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
        topics = "${oms.kafka.topics.proposal.name}")
@Slf4j
@RequiredArgsConstructor
public class OrderProposalListener {
    private final OrderProposalService orderProposalService;
    @KafkaHandler
    public void listen(OrderProposal payload,
                       @Header(KafkaHeaders.RECEIVED_KEY) String key,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                       @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long ts) {
        log.debug("Received OrderProposal: {} ts: {} ", payload, ts);
        orderProposalService.processOrder(payload);
    }
}
