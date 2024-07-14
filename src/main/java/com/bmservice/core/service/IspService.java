package com.bmservice.core.service;

import com.bmservice.core.mapper.system.IspMapper;
import com.bmservice.core.models.Isp;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class IspService {

    private final IspMapper ispMapper;

    public List<Isp> all() {
        return ispMapper.all();
    }

    public void add(Isp isp) {
        try {
            ispMapper.save(isp);

        } catch (Exception e) {
            // duplicate
        }
    }
}
