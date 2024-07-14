package com.bmservice.core.models;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Isp {
    private Integer statusId;
    private String name;
    private Integer createdBy;
    private Integer lastUpdatedBy;
    private Date createdAt;
    private Date lastUpdatedAt;
    private String authorizedUsers;
    private Integer id;
}
