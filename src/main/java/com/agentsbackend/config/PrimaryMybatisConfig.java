package com.agentsbackend.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import com.agentsbackend.repos.PrimaryMapper;

/**
 * MyBatis configuration for primary transactional repositories.
 * Scans all repositories in com.agentsbackend.repos (excluding analytics)
 * to use the primary transactional datasource.
 */
@Configuration
@MapperScan(
    basePackages = "com.agentsbackend.repos",
    sqlSessionFactoryRef = "primarySqlSessionFactory",
    markerInterface = PrimaryMapper.class
)
public class PrimaryMybatisConfig {
    // Configuration-only class for primary repositories mapper scanning
}
