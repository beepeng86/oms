package com.abc.main.oms.service;

import com.abc.main.common.model.OrderProposal;
import com.abc.main.common.model.order.OrderCommand;
import com.abc.main.oms.config.OmsKafkaProperties;
import com.abc.main.oms.service.operation.OrderOperationService;
import com.abc.main.oms.service.operation.OrderOperationServiceSelector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderProposalService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OmsKafkaProperties omsKafkaProperties;
    private final OrderOperationServiceSelector orderOperationSelector;

    @Transactional
    public void processOrder(OrderProposal order) {
        // TODO: Put in validation logic and/or further refactor validations
        validate(order);

        OrderOperationService service = orderOperationSelector.getOrderOperationService(order.orderOperation());
        OrderCommand orderCommand = service.onProposal(order);

        kafkaTemplate.send(omsKafkaProperties.topics().get("command").name(),
                omsKafkaProperties.accounts().get(order.accountId()).partitionId(),
                omsKafkaProperties.accounts().get(order.accountId()).accountKey(),
                orderCommand);
    }

    private void validate(OrderProposal order) {
        if (order.orderOperation() == null)
            throw new IllegalArgumentException("OrderOperation is required");
    }
}
