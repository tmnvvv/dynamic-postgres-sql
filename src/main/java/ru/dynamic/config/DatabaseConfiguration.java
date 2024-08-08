package ru.dynamic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;
import java.sql.Driver;

@Configuration
public class DatabaseConfiguration {
    @Value("${spring.datasource.driver-class-name}")
    private String postgresDriverClassName;

    @Value("${spring.datasource.url}")
    private String postgresUrl;

    @Value("${spring.datasource.username}")
    private String postgresUsername;

    @Value("${spring.datasource.password}")
    private String postgresPassword;

    @Bean(name="postgres")
    public JdbcTemplate postgresJdbcTemplate() throws Exception {
        final Class<?> driverClass = ClassUtils.resolveClassName(postgresDriverClassName, this.getClass().getClassLoader());
        final Driver driver = (Driver) ClassUtils.getConstructorIfAvailable(driverClass).newInstance();
        try {
            final DataSource dataSource = new SimpleDriverDataSource(driver, postgresUrl, postgresUsername, postgresPassword);
            return new JdbcTemplate(dataSource);
        } catch (Exception e) {
            throw e;
        }
    }
}
