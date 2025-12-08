package com.abc.main.oms.service;

import com.abc.main.common.model.OpenOrder;
import com.abc.main.common.model.OrderUpdate;
import com.abc.main.common.model.Side;
import com.abc.main.common.model.entity.AccountBalance;
import com.abc.main.oms.repo.AccountBalanceHCRepo;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountBalanceService {
    private final AccountBalanceHCRepo repo;

    public void freezeBalance(OpenOrder order) {
        AccountBalance accBal = repo.findByIdAndLock(order.getKey().getAccountId());

        try {
            if (order.getSide() == Side.Buy) {
                freezeBalance(order.getQty(), order.getOriginalPrice(), accBal);
                repo.save(accBal);
            }
        } finally {
            repo.unlock(accBal);
        }
    }

    public void revertBalance(OpenOrder order) {
        revertBalance(order, order.getQty());
    }

    public void revertBalanceOnCancel(OpenOrder order) {
        revertBalance(order, order.getQty().subtract(order.getExecQty()));
    }

    private void revertBalance(OpenOrder order, BigDecimal qty) {
        AccountBalance accBal = repo.findByIdAndLock(order.getKey().getAccountId());
        try {
            if (order.getSide() == Side.Buy) {
                unfreezeBalance(qty, order.getOriginalPrice(), accBal);
                repo.save(accBal);
            }
        } finally {
            repo.unlock(accBal);
        }
    }

    public void adjustBalance(OrderUpdate orderUpdate, BigDecimal originalPrice) {
        AccountBalance accBal = repo.findByIdAndLock(orderUpdate.accountId());
        try {
            BigDecimal execOrderAmount = orderUpdate.execQty().multiply(orderUpdate.execPrice());

            switch (orderUpdate.side()) {
                case Buy -> {
                    // balance -= execQty*execPrice
                    // frozen_balance -= execQty*originalPrice
                    accBal.setCashBalance(
                        accBal.getCashBalance().subtract(execOrderAmount));

                    unfreezeBalance(orderUpdate.execQty(), originalPrice, accBal);
                }
                case Sell ->
                    // balance += execQty*execPrice
                    accBal.setCashBalance(
                        accBal.getCashBalance().add(execOrderAmount));
                case None -> {
                }
            }
            // balance -= execFee
            accBal.setCashBalance(accBal.getCashBalance().subtract(orderUpdate.execFee()));

            repo.save(accBal);
        } finally {
            repo.unlock(accBal);
        }
    }

    public void unfreezeBalance(BigDecimal qty, BigDecimal price, AccountBalance accBalance) {
        adjustFrozenBalance(qty, price, accBalance, false);
    }

    private void freezeBalance(BigDecimal qty, BigDecimal price, AccountBalance accBalance) {
        adjustFrozenBalance(qty, price, accBalance, true);
    }

    private void adjustFrozenBalance(BigDecimal qty, BigDecimal price, AccountBalance accBalance, boolean freeze) {
        BigDecimal frozenAmountPerQty = qty.multiply(price);
        BigDecimal currentFrozenBalance = accBalance.getFrozenBalance();

        BigDecimal newFrozenBalance;
        if (freeze) {
            newFrozenBalance = currentFrozenBalance.add(frozenAmountPerQty);
        } else {
            newFrozenBalance = currentFrozenBalance.subtract(frozenAmountPerQty);
        }
        accBalance.setFrozenBalance(newFrozenBalance);
    }

}
