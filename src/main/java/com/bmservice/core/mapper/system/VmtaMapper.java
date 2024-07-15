package com.bmservice.core.mapper.system;

import com.bmservice.core.models.admin.Vmta;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VmtaMapper {

    @Select("SELECT * from admin.vmtas where id IN (${vmtas})")
    List<Vmta> findAllIn(String vmtas);
}
