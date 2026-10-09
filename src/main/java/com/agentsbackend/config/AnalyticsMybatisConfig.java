package com.agentsbackend.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis configuration for analytics repository.
 * Scans and registers AnalyticsRepository to use the warehouse datasource.
 */
@Configuration
@MapperScan(
    basePackages = "com.agentsbackend.repos.analytics",
    sqlSessionFactoryRef = "warehouseSqlSessionFactory"
)
public class AnalyticsMybatisConfig {
    // Configuration-only class for analytics mapper scanning
}
