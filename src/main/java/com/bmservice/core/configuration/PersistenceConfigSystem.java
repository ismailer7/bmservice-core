package com.bmservice.core.configuration;

import lombok.AllArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@MapperScan(value = "com.bmservice.core.mapper.system", sqlSessionFactoryRef = "primarySqlSessionFactory", sqlSessionTemplateRef = "primarySqlSessionTemplate")
@AllArgsConstructor
public class PersistenceConfigSystem {

    private final DBProperties dbProperties;


    @Bean(name = "system")
    @Primary
    public DataSource primaryDataSource() {
        var uri = dbProperties.getUri() + dbProperties.getPrimaryDatasource();
        return new DriverManagerDataSource(uri, dbProperties.getUsername(), dbProperties.getPassword());
    }


    @Bean(name = "primarySqlSessionFactory")
    @Primary
    public SqlSessionFactoryBean sqlSessionFactoryBean() {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(primaryDataSource());
        return factoryBean;
    }

    @Bean(name = "primaryTransactionManager")
    @Primary
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(primaryDataSource());
    }

    @Bean(name = "primarySqlSessionTemplate")
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("primarySqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
