package com.bmservice.core.controller;

import com.bmservice.core.service.IspService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/isp")
@RestController
@AllArgsConstructor
public class IspController {

    private final IspService ispService;

   /* @GetMapping("/all")
    public List<Isp> all() {
        return ispService.all();
    }

    @PostMapping("/add")
    public void add(@RequestBody @Valid IspDto ispDto) {
        ispService.add(Isp.builder().statusId(1).name(ispDto.getName()).createdBy(1).lastUpdatedBy(1).createdAt(new Date()).lastUpdatedAt(new Date()).authorizedUsers(null).build());
    }

    public void edit() {

    }

    public void delete() {

    }

    public void read() {

    }*/

}
