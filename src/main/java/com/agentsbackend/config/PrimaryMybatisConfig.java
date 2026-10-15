package com.agentsbackend.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis configuration for primary transactional repositories.
 * Scans only transactional repositories
 * to use the primary transactional datasource.
 */
@Configuration
@MapperScan(
    basePackages = "com.agentsbackend.repos.transactional",
    sqlSessionFactoryRef = "primarySqlSessionFactory"
)
public class PrimaryMybatisConfig {
    // Configuration-only class for primary repositories mapper scanning
}
