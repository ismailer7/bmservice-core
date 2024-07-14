package com.bmservice.core.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class DBProperties {

    @Value("${bmservice.datasource.uri}")
    private String uri;

    @Value("${bmservice.datasource.system}")
    private String primaryDatasource;

    @Value("${bmservice.datasource.list}")
    private String secondaryDatasource;

    @Value("${bmservice.db.username}")
    private String username;

    @Value("${bmservice.db.password}")
    private String password;

}
