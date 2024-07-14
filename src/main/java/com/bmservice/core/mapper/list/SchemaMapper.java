package com.bmservice.core.mapper.list;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SchemaMapper {

    @Update("CREATE SCHEMA ${schema} AUTHORIZATION admin;")
    void addSchema(String schema);

}
