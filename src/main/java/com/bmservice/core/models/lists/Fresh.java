package com.bmservice.core.models.lists;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Fresh {

    private int id;
    private String email;
    private String fname;
    private String lname;
    private String offersExcluded;

}
