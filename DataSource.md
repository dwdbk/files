Sure! Below is the complete Java code structure for a Spring Boot project using multiple datasources and stored procedures, based on the SqlStep and MultiStepSqlExecutor design you shared.


---

1. Enum: StepMode.java

package com.example.executor;

public enum StepMode {
    CALL_SQL, SIMPLE_JDBC_CALL
}


---

2. POJO: SqlStep.java

package com.example.executor;

import org.springframework.jdbc.core.RowMapper;
import java.util.Map;
import java.util.function.Function;

public class SqlStep<T, I> {
    private final StepMode mode;
    private final String sqlFileOrProcName;
    private final RowMapper<T> rowMapper;
    private final Function<I, Map<String, Object>> paramMapper;
    private final boolean isList;
    private final String resultKey;
    private final String dataSourceBeanName;

    public SqlStep(StepMode mode, String sqlFileOrProcName, RowMapper<T> rowMapper,
                   Function<I, Map<String, Object>> paramMapper, boolean isList,
                   String resultKey, String dataSourceBeanName) {
        this.mode = mode;
        this.sqlFileOrProcName = sqlFileOrProcName;
        this.rowMapper = rowMapper;
        this.paramMapper = paramMapper;
        this.isList = isList;
        this.resultKey = resultKey;
        this.dataSourceBeanName = dataSourceBeanName;
    }

    public StepMode getMode() { return mode; }
    public String getSqlFileOrProcName() { return sqlFileOrProcName; }
    public RowMapper<T> getRowMapper() { return rowMapper; }
    public Function<I, Map<String, Object>> getParamMapper() { return paramMapper; }
    public boolean isList() { return isList; }
    public String getResultKey() { return resultKey; }
    public String getDataSourceBeanName() { return dataSourceBeanName; }
}


---

3. SqlFileReader.java

package com.example.executor;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class SqlFileReader {
    public String readSql(String filename) throws IOException {
        Path path = new ClassPathResource("sql/" + filename).getFile().toPath();
        return Files.readString(path);
    }
}


---

4. MultiStepSqlExecutor.java

package com.example.executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class MultiStepSqlExecutor {

    private final SqlFileReader sqlFileReader;
    private final ApplicationContext applicationContext;

    @Autowired
    public MultiStepSqlExecutor(SqlFileReader sqlFileReader, ApplicationContext applicationContext) {
        this.sqlFileReader = sqlFileReader;
        this.applicationContext = applicationContext;
    }

    public <T> T executeSteps(List<SqlStep<?, ?>> steps) throws IOException {
        Object input = null;
        Object output = null;

        for (SqlStep<?, ?> step : steps) {
            Map<String, Object> params = step.getParamMapper().apply(input);
            DataSource dataSource = applicationContext.getBean(step.getDataSourceBeanName(), DataSource.class);
            NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

            if (step.getMode() == StepMode.CALL_SQL) {
                String sql = sqlFileReader.readSql(step.getSqlFileOrProcName());
                if (step.isList()) {
                    List<?> resultList = jdbcTemplate.query(sql, new MapSqlParameterSource(params), step.getRowMapper());
                    input = resultList;
                    output = resultList;
                } else {
                    Object result = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(params), step.getRowMapper());
                    input = result;
                    output = result;
                }
            } else if (step.getMode() == StepMode.SIMPLE_JDBC_CALL) {
                SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
                        .withProcedureName(step.getSqlFileOrProcName())
                        .returningResultSet(step.getResultKey(), step.getRowMapper());

                Map<String, Object> result = jdbcCall.execute(params);
                Object mappedResult = result.get(step.getResultKey());

                if (step.isList()) {
                    input = mappedResult;
                    output = mappedResult;
                } else {
                    List<?> list = (List<?>) mappedResult;
                    Object first = list.isEmpty() ? null : list.get(0);
                    input = first;
                    output = first;
                }
            }
        }

        return (T) output;
    }
}


---

5. MultiDataSourceConfig.java

package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class MultiDataSourceConfig {

    @Bean(name = "usersDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.users")
    public DataSource usersDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "ordersDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.orders")
    public DataSource ordersDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "productsDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.products")
    public DataSource productsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "paymentsDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.payments")
    public DataSource paymentsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "inventoryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.inventory")
    public DataSource inventoryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "notificationsDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.notifications")
    public DataSource notificationsDataSource() {
        return DataSourceBuilder.create().build();
    }
}


---

6. application.properties

spring.datasource.users.url=jdbc:postgresql://localhost:5432/usersdb
spring.datasource.users.username=postgres
spring.datasource.users.password=password
spring.datasource.users.driver-class-name=org.postgresql.Driver

spring.datasource.orders.url=jdbc:postgresql://localhost:5432/ordersdb
spring.datasource.orders.username=postgres
spring.datasource.orders.password=password
spring.datasource.orders.driver-class-name=org.postgresql.Driver

spring.datasource.products.url=jdbc:postgresql://localhost:5432/productsdb
spring.datasource.products.username=postgres
spring.datasource.products.password=password
spring.datasource.products.driver-class-name=org.postgresql.Driver

spring.datasource.payments.url=jdbc:postgresql://localhost:5432/paymentsdb
spring.datasource.payments.username=postgres
spring.datasource.payments.password=password
spring.datasource.payments.driver-class-name=org.postgresql.Driver

spring.datasource.inventory.url=jdbc:postgresql://localhost:5432/inventorydb
spring.datasource.inventory.username=postgres
spring.datasource.inventory.password=password
spring.datasource.inventory.driver-class-name=org.postgresql.Driver

spring.datasource.notifications.url=jdbc:postgresql://localhost:5432/notificationsdb
spring.datasource.notifications.username=postgres
spring.datasource.notifications.password=password
spring.datasource.notifications.driver-class-name=org.postgresql.Driver


---

Let me know if you also want the RowMapper classes or a controller to run this setup with real endpoints.

