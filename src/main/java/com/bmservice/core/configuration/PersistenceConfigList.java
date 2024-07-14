package com.bmservice.core.configuration;

import lombok.AllArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan("com.bmservice.core.mapper.list")
@AllArgsConstructor
public class PersistenceConfigList {

    private final DBProperties dbProperties;

    @Bean(name = "list")
    public DataSource dataSource2() {
        var uri = dbProperties.getUri() + dbProperties.getSecondaryDatasource();
        return new DriverManagerDataSource(uri, dbProperties.getUsername(), dbProperties.getPassword());
    }

    @Bean
    public SqlSessionFactory secondarySqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource2());
        return factoryBean.getObject();
    }

    @Bean(name = "secondaryTransactionManager")
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource2());
    }

}
