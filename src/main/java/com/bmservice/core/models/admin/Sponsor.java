package com.bmservice.core.models.admin;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Sponsor {

    private int id;
    private int statusId;
    private int affiliateId;
    private String name;
    private String website;
    private String username;
    private String password;
    private String apiKey;
    private String apiUrl;
    private String apiType;
    private int createdBy;
    private int lastUpdatedBy;
    private Date createdAt;
    private Date lastUpdatedAt;
}
