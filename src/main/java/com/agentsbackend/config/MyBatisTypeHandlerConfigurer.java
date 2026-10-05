package com.agentsbackend.config;

import com.agentsbackend.enums.*;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Register all custom type handlers with MyBatis SessionFactory.
 * Handles conversion between Java types and PostgreSQL native types.
 */
@Component
public class MyBatisTypeHandlerConfigurer implements BeanPostProcessor {

    // Placeholder for pre-initialization processing of beans
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    // Registers all custom type handlers with MyBatis when SqlSessionFactory is initialized
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SqlSessionFactory) {
            SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) bean;
            var registry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();
            
            // UUID type handler
            registry.register(UUID.class, JdbcType.VARCHAR, new UUIDTypeHandler());
            
            // Enum type handlers (9 total)
            registry.register(AdminRole.class, new AdminRoleTypeHandler());
            registry.register(AccountStatus.class, new AccountStatusTypeHandler());
            registry.register(OrderStatus.class, new OrderStatusTypeHandler());
            registry.register(OrderType.class, new OrderTypeTypeHandler());
            registry.register(TransactionType.class, new TransactionTypeTypeHandler());
            registry.register(AssetClass.class, new AssetClassTypeHandler());
            registry.register(PortfolioSizeRange.class, new PortfolioSizeRangeTypeHandler());
            registry.register(RiskTolerance.class, new RiskToleranceTypeHandler());
        }
        return bean;
    }
}
