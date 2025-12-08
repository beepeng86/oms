package com.abc.main.oms.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.abc.main.common.model.OpenOrder;
import com.abc.main.common.model.OrderOperation;
import com.abc.main.common.model.OrderProposal;
import com.abc.main.common.model.OrderStatus;
import com.abc.main.common.model.OrderUpdate;
import com.abc.main.common.model.Side;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
class OrderUpdateServiceTest {
    @Autowired
    private OrderUpdateService orderUpdateService;
    @Autowired
    private OpenOrdersHCService openOrderService;
    @MockBean
    private AccountBalanceService accountBalanceService;
    @MockBean
    private InventoryService inventoryService;
    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void processOrder_partialFilledOrder() {
        OrderUpdate order = OrderUpdate.builder()
                .orderId("instID123" + "|" + UUID.randomUUID())
                .accountId("account1")
                .symbol("symbol")
                .instructionId("instID123")
                .price(BigDecimal.valueOf(123.45))
                .qty(BigDecimal.valueOf(100L))
                .execQty(BigDecimal.valueOf(50L))
                .leavesQty(BigDecimal.valueOf(50L))
                .build();

        // create initial open order for update
        openOrderService.createOpenOrder(OrderProposal.builder()
                .orderId(order.orderId())
                .accountId("account1")
                .symbol("symbol")
                .instructionId("instID123")
                .side(Side.Buy)
                .price(BigDecimal.valueOf(123.45))
                .qty(BigDecimal.valueOf(200L))
                .orderOperation(OrderOperation.CREATE)
                .build());

        orderUpdateService.processOrder(order);

        // get current image of open order after update for assertion
        OpenOrder openOrder = openOrderService.getOpenOrder(order);
        assertThat(openOrder.getOrderStatus()).isEqualTo(OrderStatus.PARTIALLY_FILLED.name());
        assertThat(openOrder.getExecQty()).isLessThan(openOrder.getQty());
    }

    @Test
    void processOrder_FilledOrder() {
        OrderUpdate order = OrderUpdate.builder()
                .orderId(String.valueOf(UUID.randomUUID()))
                .orderId("instID123" + "|" + UUID.randomUUID())
                .accountId("account1")
                .symbol("symbol")
                .instructionId("instID123")
                .price(BigDecimal.valueOf(123.45))
                .qty(BigDecimal.valueOf(150L))
                .execQty(BigDecimal.valueOf(50L))
                .leavesQty(BigDecimal.ZERO)
                .build();

        // create initial open order for update
        OpenOrder openOrder = openOrderService.createOpenOrder(OrderProposal.builder()
                .orderId(order.orderId())
                .accountId("account1")
                .symbol("symbol")
                .instructionId("instID123")
                .side(Side.Buy)
                .price(BigDecimal.valueOf(123.45))
                .qty(BigDecimal.valueOf(150L))
                .orderOperation(OrderOperation.CREATE)
                .build());

        // simulate that 100 already being executed prior
        openOrder.setExecQty(BigDecimal.valueOf(100L));
        openOrder.setOrderStatus(OrderStatus.ACTIVE.name());
        openOrderService.update(openOrder);

        orderUpdateService.processOrder(order);

        // get current image of open order after update for assertion
        // Filled order will be remove from open order list
        OpenOrder openOrderResult = openOrderService.getOpenOrder(order);
        assertThat(openOrderResult).isNull();
    }

    @Test
    void testProcessOrder_WithException() {
        // verify transaction rollback
    }
}