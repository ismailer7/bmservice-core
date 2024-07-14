package com.bmservice.core.configuration;

import lombok.AllArgsConstructor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan("com.bmservice.core.mapper.system")
@AllArgsConstructor
public class PersistenceConfigSystem {

    private final DBProperties dbProperties;


    @Bean(name = "system")
    public DataSource primaryDataSource() {
        var uri = dbProperties.getUri() + dbProperties.getPrimaryDatasource();
        return new DriverManagerDataSource(uri, dbProperties.getUsername(), dbProperties.getPassword());
    }

    @Primary
    @Bean(name = "primarySqlSessionFactory")
    public SqlSessionFactoryBean sqlSessionFactoryBean() {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(primaryDataSource());
        return factoryBean;
    }

    @Bean(name = "primaryTransactionManager")
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(primaryDataSource());
    }

}
