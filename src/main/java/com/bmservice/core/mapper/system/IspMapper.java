package com.bmservice.core.mapper.system;

import com.bmservice.core.models.admin.Isp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IspMapper {


    @Select("SELECT * FROM admin.isps")
    @Results(value = {
            @Result(property = "statusId", column = "status_id"),
            @Result(property = "name", column = "name"),
            @Result(property = "createdBy", column = "created_by"),
            @Result(property = "lastUpdatedBy", column = "last_updated_by"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "lastUpdatedAt", column = "last_updated_at"),
            @Result(property = "authorizedUsers", column = "authorized_users"),
            @Result(property = "id", column = "id")
    })
    List<Isp> all();

    /*
    @Insert(value = "INSERT INTO admin.isps(status_id, name, created_by, last_updated_by, created_at, last_updated_at, authorized_users) VALUES(#{statusId}, #{name}, #{createdBy}, #{lastUpdatedBy}, #{createdAt}, #{lastUpdatedAt}, #{authorizedUsers} )")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void save(Isp isp);*/

}
