package com.bmservice.core.mapper.list;

import com.bmservice.core.models.admin.Server;
import com.bmservice.core.models.lists.Fresh;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface ListMapper {

    final String sql = "${sql}";

    @Select(sql)
    List<Fresh> execute(HashMap<String, String> m);

}
