package com.agentsbackend.config;

import com.agentsbackend.enums.AdminRole;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Register custom type handlers with MyBatis SessionFactory
 */
@Component
public class MyBatisTypeHandlerConfigurer implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SqlSessionFactory) {
            SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) bean;
            // Register UUID type handler
            sqlSessionFactory.getConfiguration().getTypeHandlerRegistry()
                    .register(UUID.class, JdbcType.VARCHAR, new UUIDTypeHandler());
            // Register AdminRole type handler
            sqlSessionFactory.getConfiguration().getTypeHandlerRegistry()
                    .register(AdminRole.class, new AdminRoleTypeHandler());
        }
        return bean;
    }
}
