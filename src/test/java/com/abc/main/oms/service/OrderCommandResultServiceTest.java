package com.abc.main.oms.service;

import com.abc.main.common.model.OpenOrder;
import com.abc.main.common.model.OrderCommandResult;
import com.abc.main.common.model.OrderOperation;
import com.abc.main.common.model.OrderProposal;
import com.abc.main.common.model.OrderStatus;
import com.abc.main.common.model.Side;
import com.abc.main.common.model.*;
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
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
class OrderCommandResultServiceTest {

    @Autowired
    private OrderCommandResultService orderCommandResultService;
    @Autowired
    private OpenOrdersHCService openOrderService;
    @MockBean
    private AccountBalanceService accountBalanceService;
    @MockBean
    private InventoryService inventoryService;
    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void testProcessOrder_withCreateOrder_SuccessfulExecution() {
        OrderCommandResult orderCommandResult = OrderCommandResult.builder()
                .orderId("instID123" + "|" + UUID.randomUUID())
                .accountId("account1")
                .symbol("symbol")
                .instructionId("instID123")
                .executionStatus("Success")
                .build();

        // create initial open order for update
        openOrderService.createOpenOrder(OrderProposal.builder()
                .orderId(orderCommandResult.orderId())
                .accountId("account1")
                .symbol("symbol")
                .instructionId("instID123")
                .side(Side.Buy)
                .price(BigDecimal.valueOf(123.45))
                .qty(BigDecimal.valueOf(100L))
                .orderOperation(OrderOperation.CREATE)
                .build());

        orderCommandResultService.processOrder(orderCommandResult);

        // get current image of open orderCommandResult after update for assertion
        OpenOrder openOrder = openOrderService.getOpenOrder(orderCommandResult);
        assertThat(openOrder.getOrderStatus()).isEqualTo(OrderStatus.ACTIVE.name());
    }

    @Test
    void testProcessOrder_withCreateOrder_FailExecution() {

        OrderCommandResult orderCommandResult = OrderCommandResult.builder()
                .orderId("instID123" + "|" + UUID.randomUUID())
                .accountId("account1")
                .symbol("symbol")
                .instructionId("instID123")
                .executionStatus("Fail")
                .build();

        // create initial open order for update
        openOrderService.createOpenOrder(OrderProposal.builder()
                .orderId(orderCommandResult.orderId())
                .accountId("account1")
                .symbol("symbol")
                .instructionId("instID123")
                .side(Side.Buy)
                .price(BigDecimal.valueOf(123.45))
                .qty(BigDecimal.valueOf(100L))
                .orderOperation(OrderOperation.CREATE)
                .build());

        orderCommandResultService.processOrder(orderCommandResult);

        // get current image of open order after update for assertion
        OpenOrder openOrder = openOrderService.getOpenOrder(orderCommandResult);
        assertThat(openOrder.getOrderStatus()).isEqualTo(OrderStatus.FAILED.name());

        // Verify interactions
        verify(accountBalanceService).revertBalance(any());
        verify(inventoryService).revertFrozenInventory(any());
    }

    @Test
    void testProcessOrder_withCancelOrder_SuccessfulExecution() {
        OrderCommandResult orderCommandResult = OrderCommandResult.builder()
                .orderId("instID123" + "|" + UUID.randomUUID())
                .accountId("account1")
                .symbol("symbol")
                .instructionId("instID123")
                .brokerOrderId("brokerOrderId123")
                .executionStatus("Success")
                .build();

        // create initial open order for update
        OpenOrder openOrderCreate = openOrderService.createOpenOrder(OrderProposal.builder()
                .orderId("instID123" + "|" + UUID.randomUUID())
                .accountId("account1")
                .symbol("symbol")
                .instructionId("instID123")
                .side(Side.Buy)
                .price(BigDecimal.valueOf(123.45))
                .qty(BigDecimal.valueOf(100L))
                .orderOperation(OrderOperation.CREATE)
                .build());
        openOrderCreate.setOrderStatus(OrderStatus.ACTIVE.name());
        openOrderCreate.setBrokerOrderId("brokerOrderId123");
        openOrderService.update(openOrderCreate);

        // create cancel open order for command update
        openOrderService.createOpenOrder(OrderProposal.builder()
                .orderId(orderCommandResult.orderId())
                .brokerOrderId("brokerOrderId123")
                .accountId("account1")
                .symbol("symbol")
                .instructionId("instID123")
                .side(Side.Buy)
                .price(BigDecimal.valueOf(123.45))
                .qty(BigDecimal.valueOf(100L))
                .orderOperation(OrderOperation.CANCEL)
                .build());

        orderCommandResultService.processOrder(orderCommandResult);

        // get current image of open order after commandResult for assertion
        OpenOrder openOrder = openOrderService.getOpenOrder(orderCommandResult);
        openOrderCreate = openOrderService.getOpenOrder(OrderCommandResult.builder()
                .orderId(openOrderCreate.getOrderId())
                .accountId("account1")
                .symbol("symbol")
                .instructionId("instID123")
                .build());
        assertThat(openOrder).isNull();
        assertThat(openOrderCreate).isNull();

        // Verify interactions
        verify(accountBalanceService).revertBalanceOnCancel(any());
        verify(inventoryService).revertFrozenInventoryOnCancel(any());
    }

    @Test
    void testProcessOrder_withCancel_FailExecution() {
        OrderCommandResult orderCommandResult = OrderCommandResult.builder()
                .orderId("instID123" + "|" + UUID.randomUUID())
                .accountId("account1")
                .symbol("symbol")
                .instructionId("instID123")
                .brokerOrderId("brokerOrderId123")
                .executionStatus("Fail")
                .build();

        // create initial open order for update
        OpenOrder openOrderCreate = openOrderService.createOpenOrder(OrderProposal.builder()
                .orderId("instID123" + "|" + UUID.randomUUID())
                .accountId("account1")
                .symbol("symbol")
                .instructionId("instID123")
                .side(Side.Buy)
                .price(BigDecimal.valueOf(123.45))
                .qty(BigDecimal.valueOf(100L))
                .orderOperation(OrderOperation.CREATE)
                .build());
        openOrderCreate.setOrderStatus(OrderStatus.ACTIVE.name());
        openOrderCreate.setBrokerOrderId("brokerOrderId123");
        openOrderService.update(openOrderCreate);

        // create cancel open order for command update
        openOrderService.createOpenOrder(OrderProposal.builder()
                .orderId(orderCommandResult.orderId())
                .brokerOrderId("brokerOrderId123")
                .accountId("account1")
                .symbol("symbol")
                .instructionId("instID123")
                .side(Side.Buy)
                .price(BigDecimal.valueOf(123.45))
                .qty(BigDecimal.valueOf(100L))
                .orderOperation(OrderOperation.CANCEL)
                .build());

        orderCommandResultService.processOrder(orderCommandResult);

        // get current image of open order after commandResult for assertion
        OpenOrder openOrder = openOrderService.getOpenOrder(orderCommandResult);
        openOrderCreate = openOrderService.getOpenOrder(OrderCommandResult.builder()
                .orderId(openOrderCreate.getOrderId())
                .accountId("account1")
                .symbol("symbol")
                .instructionId("instID123")
                .build());
        assertThat(openOrder).isNull();
        assertThat(openOrderCreate).isNotNull();
    }

    @Test
    void testProcessOrder_withUpdate_SuccessfulExecution() {}
    @Test
    void testProcessOrder_withUpdate_FailExecution() {}
    @Test
    void testProcessOrder_WithException() {
        // verify transaction rollback
    }
}