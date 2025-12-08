package com.abc.main.oms.service.operation;

import com.abc.main.common.model.OrderOperation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class OrderOperationServiceSelector implements ApplicationContextAware, InitializingBean {
    private ApplicationContext applicationContext;
    protected Map<OrderOperation, OrderOperationService> orderOperationServiceMap = new EnumMap<>(OrderOperation.class);
    @Override
    public void afterPropertiesSet() {
        initOrderOperationServiceBean();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public OrderOperationService getOrderOperationService(OrderOperation operation) {
        return orderOperationServiceMap.get(operation);
    }

    private void initOrderOperationServiceBean() {
        for(OrderOperation operation : OrderOperation.values())
            orderOperationServiceMap.put(operation,
                    (OrderOperationService) applicationContext.getBean(operation.getServiceClass()));
    }
}
