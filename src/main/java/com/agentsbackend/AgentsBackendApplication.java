package com.agentsbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(exclude = { 
    HibernateJpaAutoConfiguration.class,
    JpaRepositoriesAutoConfiguration.class
})
@MapperScan("com.agentsbackend.repos")
public class AgentsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentsBackendApplication.class, args);
    }
}
