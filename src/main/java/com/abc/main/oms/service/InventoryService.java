package com.abc.main.oms.service;

import com.abc.main.common.model.OpenOrder;
import com.abc.main.common.model.Side;
import com.abc.main.common.model.entity.Inventory;
import com.abc.main.common.model.entity.InventoryKey;
import com.abc.main.oms.repo.InventoryHCRepo;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryHCRepo repo;

    public void freezeInventory(OpenOrder order) {
        InventoryKey key = InventoryKey.builder()
                .accountId(order.getKey().getAccountId())
                .symbol(order.getKey().getSymbol())
                .build();

        Inventory inventory = repo.findByKeyAndLock(key);

        try {
            if (order.getSide() == Side.Sell) {
                // qty untouch
                // frozen_qty += orderQty
                inventory.setFrozenQty(inventory.getFrozenQty().add(order.getQty()));
                repo.save(inventory);
            }
        } finally {
            repo.unlock(inventory);
        }
    }

    public void revertFrozenInventory(OpenOrder order) {
        revertFrozenInventory(order, order.getQty());
    }

    public void revertFrozenInventoryOnCancel(OpenOrder order) {
        revertFrozenInventory(order, order.getQty().subtract(order.getExecQty()));
    }

    private void revertFrozenInventory(OpenOrder order, BigDecimal qty) {
        InventoryKey key = InventoryKey.builder()
                .accountId(order.getKey().getAccountId())
                .symbol(order.getKey().getSymbol())
                .build();

        Inventory inventory = repo.findByKeyAndLock(key);

        try {
            if (order.getSide() == Side.Sell) {
                // frozen_qty -= orderQty
                unfreezeInventory(inventory, qty);
                repo.save(inventory);
            }
        } finally {
            repo.unlock(inventory);
        }
    }

    public void adjustInventory(final OpenOrder openOrder,
                                final BigDecimal execQty) {
        InventoryKey key = InventoryKey.builder()
                .accountId(openOrder.getKey().getAccountId())
                .symbol(openOrder.getKey().getSymbol())
                .build();

        Inventory inventory = repo.findByKeyAndLock(key);
        try {
            switch (openOrder.getSide()) {
                case Buy ->
                    // qty += execQty
                    inventory.setQty(inventory.getQty().add(execQty));
                case Sell -> {
                    // qty -= execQty
                    // frozen_qty -= execQty
                    inventory.setQty(inventory.getQty().subtract(execQty));
                    unfreezeInventory(inventory, execQty);
                }
                case None -> {
                }
            }
            repo.save(inventory);
        } finally {
            repo.unlock(inventory);
        }
    }

    private void unfreezeInventory(Inventory inventory, BigDecimal qty) {
        inventory.setFrozenQty(inventory.getFrozenQty().subtract(qty));
    }
}
