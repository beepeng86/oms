package com.abc.main.oms.service;

import com.abc.main.common.model.OpenOrder;
import com.abc.main.common.model.OpenOrderKey;
import com.abc.main.common.model.OrderUpdate;
import com.abc.main.common.model.Side;
import com.abc.main.common.model.entity.Inventory;
import com.abc.main.oms.repo.InventoryHCRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class InventoryServiceTest {

    @InjectMocks
    private InventoryService inventoryService;

    @Mock
    private InventoryHCRepo inventoryRepository;
    OpenOrderKey openOrderKey = OpenOrderKey.builder()
            .accountId("account1")
            .symbol("symbol")
            .instructionId("instID123")
            .build();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static Stream<Arguments> provideFreezeInventoryParam() {
        return Stream.of(
                Arguments.of(10, Side.Sell, 200, 210),
                Arguments.of(10,Side.Buy,200,10),
                Arguments.of(0, Side.Sell, 200, 200),
                Arguments.of(0,Side.Buy,200,0)
        );
    }

    @ParameterizedTest
    @MethodSource("provideFreezeInventoryParam")
    void testFreezeInventory(double curentFrozenQty,
                           Side side,
                           double orderQuantity,
                           double expectedQuantity) {
        Inventory inventory = Inventory.builder()
                .qty(BigDecimal.valueOf(123000))
                .frozenQty(BigDecimal.valueOf(curentFrozenQty))
                .build();

        OpenOrder openOrder = OpenOrder.builder()
                .key(openOrderKey)
                .side(side)
                .originalPrice(BigDecimal.valueOf(12.55))
                .qty(BigDecimal.valueOf(orderQuantity))
                .build();

        when(inventoryRepository.findByKeyAndLock(any())).thenReturn(inventory);

        inventoryService.freezeInventory(openOrder);

        assertThat(BigDecimal.valueOf(expectedQuantity))
                .usingComparator(BigDecimal::compareTo)
                .isEqualTo(inventory.getFrozenQty());
    }

    private static Stream<Arguments> provideRevertFrozenInventoryParam() {
        return Stream.of(
                Arguments.of(250, Side.Sell, 200, 50),
                Arguments.of(250, Side.Buy, 200, 250),
                Arguments.of(200, Side.Sell, 200, 0),
                Arguments.of(200, Side.Buy, 200, 200)
        );
    }
    @ParameterizedTest
    @MethodSource("provideRevertFrozenInventoryParam")
    void testRevertFrozenInventory(double curentFrozenQty,
                                 Side side,
                                 double orderQuantity,
                                 double expectedQuantity) {
        Inventory inventory = Inventory.builder()
                .qty(BigDecimal.valueOf(123000))
                .frozenQty(BigDecimal.valueOf(curentFrozenQty))
                .build();

        OpenOrder openOrder = OpenOrder.builder()
                .key(openOrderKey)
                .side(side)
                .originalPrice(BigDecimal.valueOf(12.55))
                .qty(BigDecimal.valueOf(orderQuantity))
                .build();

        when(inventoryRepository.findByKeyAndLock(any())).thenReturn(inventory);

        inventoryService.revertFrozenInventory(openOrder);

        assertThat(BigDecimal.valueOf(expectedQuantity))
                .usingComparator(BigDecimal::compareTo)
                .isEqualTo(inventory.getFrozenQty());
    }

    private static Stream<Arguments> provideAdjustInventoryParam() {
        return Stream.of(
                Arguments.of(250, 123_000, Side.Sell, 200, 50, 122_800),
                Arguments.of(250, 123_000, Side.Buy, 200, 250, 123_200),
                Arguments.of(400, 123_000, Side.Sell, 250, 150, 122_750),
                Arguments.of(0, 123_000, Side.Buy, 200, 0, 123_200)
        );
    }
    @ParameterizedTest
    @MethodSource("provideAdjustInventoryParam")
    void testAdjustInventory(double curentFrozenQty,
                             double currentQty,
                             Side side,
                             double execQty,
                             double expectedFrozenQty,
                             double expectedQty) {
        Inventory inventory = Inventory.builder()
                .qty(BigDecimal.valueOf(currentQty))
                .frozenQty(BigDecimal.valueOf(curentFrozenQty))
                .build();

        OpenOrder openOrder = OpenOrder.builder()
                .key(openOrderKey)
                .side(side)
                .originalPrice(BigDecimal.valueOf(12.55))
                .qty(BigDecimal.valueOf(400))
                .build();

        OrderUpdate orderUpdate = OrderUpdate.builder()
                .execQty(BigDecimal.valueOf(execQty))
                .build();

        when(inventoryRepository.findByKeyAndLock(any())).thenReturn(inventory);

        inventoryService.adjustInventory(openOrder, orderUpdate.execQty());

        assertThat(BigDecimal.valueOf(expectedFrozenQty))
                .usingComparator(BigDecimal::compareTo)
                .isEqualTo(inventory.getFrozenQty());
        assertThat(BigDecimal.valueOf(expectedQty))
                .usingComparator(BigDecimal::compareTo)
                .isEqualTo(inventory.getQty());
    }
}