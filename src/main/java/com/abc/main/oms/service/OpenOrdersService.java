package com.abc.main.oms.service;

import com.abc.main.common.model.OpenOrder;
import com.abc.main.common.model.OpenOrderKey;
import com.abc.main.common.model.OrderInfo;
import com.abc.main.common.model.OrderProposal;
import com.abc.main.common.model.OrderUpdate;
import com.abc.main.common.model.*;

import java.util.Optional;

public interface OpenOrdersService {
    void printAll(OpenOrderKey key);
    void removeOrder(OpenOrder order);
    OpenOrder getOpenOrder(OrderInfo order);
    OpenOrder createOpenOrder(OrderProposal order);
    void update(OpenOrder openOrder);
    Optional<OpenOrder> findCorrespondingCreateOrder(OpenOrder cancelOrder);
    void updateOpenOrderWithOrderUpdate(OpenOrder openOrder, OrderUpdate order);
}
