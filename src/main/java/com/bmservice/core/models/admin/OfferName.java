package com.bmservice.core.models.admin;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class OfferName {
    private int id;
    private int statusId;
    private int offerId;
    private String value;
    private int createdBy;
    private int lastUpdatedBy;
    private Date createdAt;
    private Date lastUpdatedAt;
}
