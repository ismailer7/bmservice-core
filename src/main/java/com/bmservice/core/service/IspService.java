package com.bmservice.core.service;

import com.bmservice.core.mapper.list.SchemaMapper;
import com.bmservice.core.mapper.system.IspMapper;
import com.bmservice.core.models.Isp;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class IspService {

    private final IspMapper ispMapper;
    private final SchemaMapper schemaMapper;

    public List<Isp> all() {
        return ispMapper.all();
    }

    public void add(Isp isp) {
        try {
            ispMapper.save(isp);
            schemaMapper.addSchema(isp.getName());
        } catch (Exception e) {
            // duplicate
            schemaMapper.addSchema(isp.getName());
        }
    }
}
