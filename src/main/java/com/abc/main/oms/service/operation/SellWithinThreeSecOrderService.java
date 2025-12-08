package com.abc.main.oms.service.operation;

import com.abc.main.common.model.OpenOrder;
import com.abc.main.common.model.OrderCommandResult;
import com.abc.main.common.model.OrderProposal;
import com.abc.main.common.model.order.CreateOrder;
import com.abc.main.common.model.order.OrderCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellWithinThreeSecOrderService implements OrderOperationService {

    @Override
    public OrderCommand onProposal(OrderProposal proposal) {
        // Fill in proper order logic
        return new CreateOrder(proposal);
    }

    @Override
    public void onSuccess(OpenOrder openOrder, OrderCommandResult order) {
        // Fill in proper order logic
    }

    @Override
    public void onFail(OpenOrder openOrder, OrderCommandResult order) {
        // Fill in proper order logic
    }
}
