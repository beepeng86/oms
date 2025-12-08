package com.abc.main.oms.service.operation;

import com.abc.main.common.model.OpenOrder;
import com.abc.main.common.model.OrderCommandResult;
import com.abc.main.common.model.OrderProposal;
import com.abc.main.common.model.order.OrderCommand;

public interface OrderOperationService {

    OrderCommand onProposal(OrderProposal proposal);
    void onSuccess(OpenOrder openOrder, OrderCommandResult order);
    void onFail(OpenOrder openOrder, OrderCommandResult order);
}
