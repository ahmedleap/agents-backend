package com.agentsbackend.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Multi-datasource configuration for transactional and warehouse databases.
 * Primary datasource: Agents platform transactional data (orders, accounts, etc.)
 * Warehouse datasource: Analytics warehouse (dimensional data, fact tables)
 */
@Configuration
public class DataSourceConfiguration {

    /**
     * Primary datasource for transactional operations
     */
    @Bean(name = "primaryDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * Warehouse datasource for analytics queries
     */
    @Bean(name = "warehouseDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.warehouse")
    public DataSource warehouseDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * SqlSessionFactory for primary transactional database
     */
    @Bean(name = "primarySqlSessionFactory")
    @Primary
    public SqlSessionFactory primarySqlSessionFactory(
            @Qualifier("primaryDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        return factoryBean.getObject();
    }

    /**
     * SqlSessionFactory for warehouse analytics database
     */
    @Bean(name = "warehouseSqlSessionFactory")
    public SqlSessionFactory warehouseSqlSessionFactory(
            @Qualifier("warehouseDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        
        // Enable mapping of snake_case SQL column aliases to camelCase Java bean properties
        // This is required for analytics queries that use aliases like 'trade_count' → 'tradeCount'
        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        config.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(config);
        
        return factoryBean.getObject();
    }
}
