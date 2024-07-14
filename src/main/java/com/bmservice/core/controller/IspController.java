package com.bmservice.core.controller;

import com.bmservice.core.models.Isp;
import com.bmservice.core.service.IspService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RequestMapping("/isp")
@RestController
@AllArgsConstructor
public class IspController {

    private final IspService ispService;

    @GetMapping("/all")
    public List<Isp> all() {
        return ispService.all();
    }

    @PostMapping("/add")
    public void add() {
        ispService.add(Isp.builder().statusId(1).name("btinternet").createdBy(1).lastUpdatedBy(1).createdAt(new Date()).lastUpdatedAt(new Date()).authorizedUsers(null).build());
    }

}
