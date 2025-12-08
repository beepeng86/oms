package com.abc.main.oms.service;

import com.abc.main.common.model.OpenOrder;
import com.abc.main.common.model.OrderOperation;
import com.abc.main.common.model.OrderProposal;
import com.abc.main.common.model.OrderStatus;
import com.abc.main.common.model.Side;
import com.abc.main.common.model.*;
import com.abc.main.common.model.order.CancelOrder;
import com.abc.main.common.model.order.CreateOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
class OrderProposalServiceTest {
    @Autowired
    private OrderProposalService orderProposalService;
    @Autowired
    private OpenOrdersHCService openOrderService;
    @MockBean
    private AccountBalanceService accountBalanceService;
    @MockBean
    private InventoryService inventoryService;
    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void processOrder_createOrder_shouldCompleteSuccessfully() throws Exception {
        OrderProposal order = OrderProposal.builder()
                .orderId("instID123" + "|" + UUID.randomUUID())
                .accountId("account1")
                .symbol("symbol")
                .instructionId("instID123")
                .side(Side.Buy)
                .price(BigDecimal.valueOf(123.45))
                .qty(BigDecimal.valueOf(200L))
                .orderOperation(OrderOperation.CREATE)
                .build();

        orderProposalService.processOrder(order);

        verify(accountBalanceService).freezeBalance(any());
        verify(inventoryService).freezeInventory(any());
        // kafka will send wrapped order
        verify(kafkaTemplate).send(eq("order-command"), eq(1), eq("account1"), any(CreateOrder.class));

        // get current image of open order after orderProposal for assertion
        OpenOrder openOrder = openOrderService.getOpenOrder(order);
        assertThat(openOrder.getOrderStatus()).isEqualTo(OrderStatus.PENDING.name());
        assertThat(openOrder.getExecQty()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void processOrder_cancelOrder_shouldCompleteSuccessfully() throws Exception {
        OrderProposal order = OrderProposal.builder()
                .orderId("instID123" + "|" + UUID.randomUUID())
                .accountId("account1")
                .symbol("symbol")
                .instructionId("instID123")
                .side(Side.Buy)
                .price(BigDecimal.valueOf(123.45))
                .qty(BigDecimal.valueOf(200L))
                .orderOperation(OrderOperation.CANCEL)
                .build();

        orderProposalService.processOrder(order);

        // kafka will send wrapped order
        verify(kafkaTemplate).send(eq("order-command"), eq(1), eq("account1"), any(CancelOrder.class));

        // get current image of open order after orderProposal for assertion
        OpenOrder openOrder = openOrderService.getOpenOrder(order);
        assertThat(openOrder.getOrderStatus()).isEqualTo(OrderStatus.PENDING.name());
        assertThat(openOrder.getExecQty()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void testProcessOrder_WithException() {
        // verify transaction rollback
    }
}