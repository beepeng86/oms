package com.abc.main.oms.service;

import com.abc.main.common.model.OpenOrder;
import com.abc.main.common.model.OpenOrderKey;
import com.abc.main.common.model.OrderUpdate;
import com.abc.main.common.model.Side;
import com.abc.main.common.model.entity.AccountBalance;
import com.abc.main.oms.repo.AccountBalanceHCRepo;
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

class AccountBalanceServiceTest {

    @InjectMocks
    private AccountBalanceService accountBalanceService;

    @Mock
    private AccountBalanceHCRepo accountBalanceRepository;
    OpenOrderKey openOrderKey = OpenOrderKey.builder()
            .accountId("account1")
            .symbol("symbol")
            .instructionId("instID123")
            .build();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static Stream<Arguments> provideFreezeBalanceParam() {
        return Stream.of(
                Arguments.of(10, Side.Sell, 200, 10),
                Arguments.of(10,Side.Buy, 200, 2520),
                Arguments.of(0, Side.Sell, 200, 0),
                Arguments.of(0, Side.Buy, 200, 2510)
        );
    }

    @ParameterizedTest
    @MethodSource("provideFreezeBalanceParam")
    void testFreezeBalance(double curentFrozenBalance,
                           Side side,
                           int orderQuantity,
                           double expectedFrozenBalance) {
        AccountBalance accountBalance = AccountBalance.builder()
                .cashBalance(BigDecimal.valueOf(123000))
                .frozenBalance(BigDecimal.valueOf(curentFrozenBalance))
                .build();

        OpenOrder openOrder = OpenOrder.builder()
                .key(openOrderKey)
                .side(side)
                .originalPrice(BigDecimal.valueOf(12.55))
                .qty(BigDecimal.valueOf(orderQuantity))
                .build();

        when(accountBalanceRepository.findByIdAndLock(any())).thenReturn(accountBalance);

        accountBalanceService.freezeBalance(openOrder);

        assertThat(BigDecimal.valueOf(expectedFrozenBalance))
                .usingComparator(BigDecimal::compareTo)
                .isEqualTo(accountBalance.getFrozenBalance());
    }

    private static Stream<Arguments> provideRevertBalanceParam() {
        return Stream.of(
                Arguments.of(250, Side.Sell, 200, 250),
                Arguments.of(2560, Side.Buy, 200, 50),
                Arguments.of(200, Side.Sell, 200, 200),
                Arguments.of(2510, Side.Buy, 200, 0)
        );
    }
    @ParameterizedTest
    @MethodSource("provideRevertBalanceParam")
    void testRevertBalance(double curentFrozenBalance,
                           Side side,
                           int orderQuantity,
                           double expectedFrozenBalance) {
        AccountBalance accountBalance = AccountBalance.builder()
                .cashBalance(BigDecimal.valueOf(123000))
                .frozenBalance(BigDecimal.valueOf(curentFrozenBalance))
                .build();

        OpenOrder openOrder = OpenOrder.builder()
                .key(openOrderKey)
                .side(side)
                .originalPrice(BigDecimal.valueOf(12.55))
                .qty(BigDecimal.valueOf(orderQuantity))
                .build();

        when(accountBalanceRepository.findByIdAndLock(any())).thenReturn(accountBalance);

        accountBalanceService.revertBalance(openOrder);

        assertThat(BigDecimal.valueOf(expectedFrozenBalance))
                .usingComparator(BigDecimal::compareTo)
                .isEqualTo(accountBalance.getFrozenBalance());
    }

    private static Stream<Arguments> provideAdjustBalance() {
        return Stream.of(
                Arguments.of(50, 123_000, Side.Sell, 200, 50, 125_499.99),
                Arguments.of(2560, 123_000, Side.Buy, 200, 50, 120_499.99),
                Arguments.of(0, 123_000, Side.Sell, 250, 0, 126_124.99),
                Arguments.of(3137.5, 123_000, Side.Buy, 250, 0, 119_874.99)
        );
    }
    @ParameterizedTest
    @MethodSource("provideAdjustBalance")
    void testAdjustBalance(double currentFrozenBalance,
                           double currentCashBalance,
                           Side side,
                           double execQty,
                           double expectedFrozenBalance,
                           double expectedCashBalance) {
        AccountBalance accountBalance = AccountBalance.builder()
                .cashBalance(BigDecimal.valueOf(currentCashBalance))
                .frozenBalance(BigDecimal.valueOf(currentFrozenBalance))
                .build();

        OpenOrder openOrder = OpenOrder.builder()
                .key(openOrderKey)
                .side(side)
                .originalPrice(BigDecimal.valueOf(12.55))
                .qty(BigDecimal.valueOf(400))
                .build();

        OrderUpdate orderUpdate = OrderUpdate.builder()
                .execPrice(BigDecimal.valueOf(12.50))
                .execQty(BigDecimal.valueOf(execQty))
                .execFee(BigDecimal.valueOf(0.01))
                .side(side)
                .build();

        when(accountBalanceRepository.findByIdAndLock(any())).thenReturn(accountBalance);

        accountBalanceService.adjustBalance(orderUpdate, openOrder.getOriginalPrice());

        assertThat(BigDecimal.valueOf(expectedCashBalance))
                .usingComparator(BigDecimal::compareTo)
                .isEqualTo(accountBalance.getCashBalance());
        assertThat(BigDecimal.valueOf(expectedFrozenBalance))
                .usingComparator(BigDecimal::compareTo)
                .isEqualTo(accountBalance.getFrozenBalance());
    }
}