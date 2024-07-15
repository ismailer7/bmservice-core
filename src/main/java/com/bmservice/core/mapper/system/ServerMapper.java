package com.bmservice.core.mapper.system;

import com.bmservice.core.models.admin.Server;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ServerMapper {

    @Select("SELECT * FROM admin.servers where id IN (${serversIds})")
    List<Server> findAllIn(String serversIds);

}
