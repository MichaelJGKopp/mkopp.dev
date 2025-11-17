package dev.mkopp.mysite.shared.config.infrastructure.postgresql;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"dev.mkopp.mysite"})
@EnableJpaAuditing
public class DatabaseConfiguration {
}
