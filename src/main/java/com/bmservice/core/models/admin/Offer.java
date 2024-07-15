package com.bmservice.core.models.admin;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Offer {

    private int id;
    private int statusId;
    private int sponsorId;
    private int productionId;
    private int dropId;
    private int verticalId;
    private String name;
    private String flag;
    private String description;
    private String rate;
    private Date launchDate;
    private Date expiringDate;
    private String rules;
    private String epc;
    private String suppressionList;
    private int createdBy;
    private int lastUpdatedBy;
    private Date createdAt;
    private Date lastUpdatedAt;
    private String authorizedUsers;
    private String key;

}
